package com.knightsofdarkness.web.monument.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.knightsofdarkness.web.common.alliance.MonumentContributionDto;
import com.knightsofdarkness.web.common.alliance.MonumentGoodsDto;
import com.knightsofdarkness.web.game.config.Monument;

class MonumentEntityTest {
    private final Monument rules = new Monument(4, new Monument.StageCost(1000, 100, 10), new Monument.Upkeep(50, 5), 0.25, 0.25, 3);

    private MonumentEntity monument()
    {
        return new MonumentEntity("Order", "Seat");
    }

    private MonumentContributionDto stages(int count)
    {
        return new MonumentContributionDto(1000 * count, 100 * count, 10 * count, 0);
    }

    @Test
    void anAssault_shouldKnockDownItsShareOfWhatStands()
    {
        var monument = monument();
        monument.pay(stages(4));
        monument.raiseStages(rules);

        assertEquals(1, monument.raze(0.25));
        assertEquals(3, monument.toDto(rules).stages());
        assertEquals(0.3, monument.raze(0.1), 0.001);
        assertEquals(2.7, monument.toDto(rules).stages());
    }

    @Test
    void aStageKnockedDown_shouldCostOnlyTheFallenShareToRebuild()
    {
        var monument = monument();
        monument.pay(stages(1));
        monument.raiseStages(rules);
        monument.raze(0.25);

        assertEquals(new MonumentGoodsDto(250, 25, 3, 0), monument.toLedgerDto(rules).nextStageCost());
    }

    @Test
    void aRebuiltStage_shouldBeNewsOnlyIfItWasWhollyLost()
    {
        var monument = monument();
        monument.pay(stages(4));
        monument.raiseStages(rules).forEach(monument::announces);

        monument.raze(0.1);
        assertFalse(monument.announces(4), "the top of the fourth stage fell, the stage still stands");

        monument.raze(0.5);
        assertFalse(monument.announces(2), "not lost");
        assertTrue(monument.announces(3), "lost whole, and rebuilt");
    }

    @Test
    void aStage_shouldRiseOnlyWhenTheStoreCoversEveryGood()
    {
        var monument = monument();

        monument.pay(new MonumentContributionDto(1000, 100, 9, 0));
        assertEquals(List.of(), monument.raiseStages(rules));

        monument.pay(new MonumentContributionDto(0, 0, 1, 0));
        assertEquals(List.of(1), monument.raiseStages(rules));

        var ledger = monument.toLedgerDto(rules);
        assertEquals(1, ledger.monument().stages());
        assertEquals(new MonumentGoodsDto(0, 0, 0, 0), ledger.store());
    }

    @Test
    void aLargePayment_shouldRaiseSeveralStagesAndStopAtTheLast()
    {
        var monument = monument();

        monument.pay(stages(6));

        assertEquals(List.of(1, 2, 3, 4), monument.raiseStages(rules));
        assertTrue(monument.isComplete(rules));
        var ledger = monument.toLedgerDto(rules);
        assertNull(ledger.nextStageCost());
        assertEquals(new MonumentGoodsDto(2000, 200, 20, 0), ledger.store());
    }

    @Test
    void theUpkeep_shouldGrowWithEveryStage()
    {
        var monument = monument();
        monument.pay(new MonumentContributionDto(2300, 200, 20, 30));
        monument.raiseStages(rules);

        assertFalse(monument.payUpkeep(rules));

        var ledger = monument.toLedgerDto(rules);
        assertEquals(new MonumentGoodsDto(100, 0, 0, 10), ledger.upkeep());
        assertEquals(new MonumentGoodsDto(200, 0, 0, 20), ledger.store());
        assertEquals(2, ledger.monument().stages());
        assertFalse(ledger.upkeepUnpaid());
    }

    @Test
    void aStoreThatCannotPayTheWholeUpkeep_shouldPayNothingAndCrumble()
    {
        var monument = monument();
        monument.pay(new MonumentContributionDto(1500, 100, 10, 0));
        monument.raiseStages(rules);

        assertTrue(monument.payUpkeep(rules), "the members should hear the first time");
        assertFalse(monument.payUpkeep(rules), "and only the first time");

        var ledger = monument.toLedgerDto(rules);
        assertEquals(0.5, ledger.monument().stages());
        assertEquals(new MonumentGoodsDto(500, 0, 0, 0), ledger.store());
        assertTrue(ledger.upkeepUnpaid());
    }

    @Test
    void aStageRepaired_shouldCostOnlyTheShareThatCrumbled()
    {
        var monument = monument();
        monument.pay(stages(1));
        monument.raiseStages(rules);
        monument.payUpkeep(rules);

        assertEquals(new MonumentGoodsDto(250, 25, 3, 0), monument.toLedgerDto(rules).nextStageCost());

        monument.pay(new MonumentContributionDto(250, 25, 3, 0));
        assertEquals(List.of(1), monument.raiseStages(rules));
        assertEquals(1, monument.toDto(rules).stages());
    }

    @Test
    void aMonumentWithNothingBuilt_shouldEatNothing()
    {
        var monument = monument();

        assertFalse(monument.payUpkeep(rules));
        assertEquals(0, monument.toDto(rules).stages());
    }

    @Test
    void aMonumentThatKeepsCrumbling_shouldStopAtNothing()
    {
        var monument = monument();
        monument.pay(stages(1));
        monument.raiseStages(rules);

        for (int tick = 0; tick < 10; tick++)
        {
            monument.payUpkeep(rules);
        }

        assertEquals(0, monument.toDto(rules).stages());
    }

    @Test
    void aStageRepaired_isNoNews_butARaisedAgainAfterFallingAWholeStageIs()
    {
        var monument = monument();
        monument.pay(stages(2));
        monument.raiseStages(rules);
        assertTrue(monument.announces(2));

        monument.payUpkeep(rules);
        assertFalse(monument.announces(2), "repaired after crumbling a little");

        monument.payUpkeep(rules);
        monument.payUpkeep(rules);
        monument.payUpkeep(rules);
        assertEquals(1, monument.toDto(rules).stages());
        assertTrue(monument.announces(2), "a whole stage lost and risen again");
    }
    private MonumentEntity complete()
    {
        var monument = monument();
        monument.pay(stages(4));
        // goods to spare in the store, for upkeep and repairs
        monument.pay(new MonumentContributionDto(10_000, 100, 10, 1000));
        monument.raiseStages(rules);
        return monument;
    }

    @Test
    void aCompleteMonument_shouldWinOnceItHasStoodTheWholeHold()
    {
        var monument = complete();
        assertEquals(3, monument.toDto(rules).ticksToWin());

        assertFalse(monument.hold(rules));
        assertFalse(monument.hold(rules));
        assertEquals(1, monument.toDto(rules).ticksToWin());
        assertTrue(monument.hold(rules));
        assertEquals(0, monument.toDto(rules).ticksToWin());
    }

    @Test
    void aMonumentNotYetComplete_shouldShowNoTimeToWin()
    {
        var monument = monument();
        monument.pay(stages(3));
        monument.raiseStages(rules);

        assertNull(monument.toDto(rules).ticksToWin());
    }

    @Test
    void aWonAssault_shouldStartTheHoldAgain()
    {
        var monument = complete();
        monument.hold(rules);
        monument.hold(rules);

        monument.raze(0.01);
        assertNull(monument.toDto(rules).ticksToWin(), "knocked below complete");
        monument.raiseStages(rules);

        assertEquals(3, monument.toDto(rules).ticksToWin(), "rebuilt, it must stand the whole hold again");
        assertFalse(monument.hold(rules));
    }

    @Test
    void aMonumentLeftUnfed_shouldCrumbleAndStartTheHoldAgain()
    {
        var monument = monument();
        monument.pay(stages(4));
        monument.raiseStages(rules);
        monument.hold(rules);

        assertTrue(monument.payUpkeep(rules));
        monument.pay(new MonumentContributionDto(10_000, 100, 10, 1000));
        monument.raiseStages(rules);

        assertEquals(3, monument.toDto(rules).ticksToWin());
    }

    @Test
    void anAssaultThatRazesNothing_shouldKeepTheHold()
    {
        var monument = complete();
        monument.hold(rules);

        assertEquals(0, monument.raze(0));
        assertEquals(2, monument.toDto(rules).ticksToWin());
    }
}
