package com.knightsofdarkness.web.kingdom.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.web.Game;
import com.knightsofdarkness.web.common.kingdom.BuildingName;
import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.kingdom.UnitName;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.legacy.TestGame;
import com.knightsofdarkness.web.utils.KingdomBuilder;

class KingdomUpkeepTest {
    private Game game;
    private GameConfig config;
    private KingdomEntity kingdom;

    @BeforeEach
    void setUp()
    {
        game = new TestGame().get();
        config = game.getConfig();
        kingdom = new KingdomBuilder(game)
                .withBuilding(BuildingName.barracks, 100)
                .withUnit(UnitName.infantry, 100)
                .withUnit(UnitName.cavalry, 50)
                .withUnit(UnitName.bowman, 0)
                .withUnit(UnitName.goldMiner, 0)
                .withResource(ResourceName.food, 1_000_000)
                .build();
    }

    private int pay(UnitName unit, int count)
    {
        return (int) Math.round(count * config.trainingCost().getTrainingCost(unit).gold() * config.common().soldierUpkeepRatio());
    }

    private KingdomTurnAction turn()
    {
        return new KingdomTurnAction(kingdom, game.getKingdomInteractor(), config, new KingdomDetailsProvider(config));
    }

    @Test
    void soldiers_shouldBePaidEveryTurn()
    {
        kingdom.getResources().setCount(ResourceName.gold, 1_000_000);
        var upkeep = pay(UnitName.infantry, 100) + pay(UnitName.cavalry, 50);

        var report = turn().passTurn(0).turnReport().orElseThrow();

        assertEquals(upkeep, report.goldUpkeep);
        assertEquals(0, report.desertedSoldiers);
        assertEquals(1_000_000 - upkeep, kingdom.getResources().getCount(ResourceName.gold));
    }

    @Test
    void soldiersAway_shouldBePaidToo()
    {
        kingdom.getResources().setCount(ResourceName.gold, 1_000_000);
        kingdom.getUnits().moveAvailableToMobile(UnitName.infantry, 40);

        var report = turn().passTurn(0).turnReport().orElseThrow();

        assertEquals(pay(UnitName.infantry, 100) + pay(UnitName.cavalry, 50), report.goldUpkeep);
    }

    @Test
    void whenTheTreasuryIsEmpty_aTenthOfTheSoldiersAtHomeShouldLeave()
    {
        kingdom.getResources().setCount(ResourceName.gold, 0);

        var report = turn().passTurn(0).turnReport().orElseThrow();

        assertEquals(0, report.goldUpkeep);
        assertEquals(15, report.desertedSoldiers);
        assertEquals(90, kingdom.getUnits().getAvailableCount(UnitName.infantry));
        assertEquals(45, kingdom.getUnits().getAvailableCount(UnitName.cavalry));
    }

    @Test
    void whenHalfThePayIsThere_fewerShouldLeave()
    {
        var upkeep = pay(UnitName.infantry, 100) + pay(UnitName.cavalry, 50);
        kingdom.getResources().setCount(ResourceName.gold, upkeep / 2);

        var report = turn().passTurn(0).turnReport().orElseThrow();

        assertEquals(upkeep / 2, report.goldUpkeep);
        assertEquals(8, report.desertedSoldiers);
        assertEquals(0, kingdom.getResources().getCount(ResourceName.gold));
    }

    @Test
    void soldiersAway_shouldNotDesert()
    {
        kingdom.getResources().setCount(ResourceName.gold, 0);
        kingdom.getUnits().moveAvailableToMobile(UnitName.infantry, 100);
        kingdom.getUnits().moveAvailableToMobile(UnitName.cavalry, 50);

        var report = turn().passTurn(0).turnReport().orElseThrow();

        assertEquals(0, report.desertedSoldiers);
        assertEquals(100, kingdom.getUnits().getMobileCount(UnitName.infantry));
    }
}
