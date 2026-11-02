package com.knightsofdarkness.web.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.web.Game;
import com.knightsofdarkness.web.alliance.model.AllianceEntity;
import com.knightsofdarkness.web.common.kingdom.AttackType;
import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.kingdom.SendAttackDto;
import com.knightsofdarkness.web.common.kingdom.UnitName;
import com.knightsofdarkness.web.common.kingdom.UnitsMapDto;
import com.knightsofdarkness.web.common.messaging.NotificationDto;
import com.knightsofdarkness.web.common.war.BattleReportDto;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.kingdom.model.KingdomDetailsProvider;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;
import com.knightsofdarkness.web.kingdom.model.KingdomMilitaryAction;
import com.knightsofdarkness.web.kingdom.model.KingdomOngoingAttackEntity;
import com.knightsofdarkness.web.kingdom.model.KingdomTurnAction;
import com.knightsofdarkness.web.legacy.TestGame;
import com.knightsofdarkness.web.utils.KingdomBuilder;

/**
 * an army that joins an ally's strike waits at the border and fights right after that army lands, however fast either ruler passes turns
 */
class StrikeTogetherTest
{
    private Game game;
    private GameConfig config;
    private KingdomDetailsProvider details;
    private KingdomEntity leader;
    private KingdomEntity joiner;
    private KingdomEntity stranger;
    private KingdomEntity target;

    @BeforeEach
    void setUp()
    {
        game = new TestGame().get();
        config = game.getConfig();
        details = new KingdomDetailsProvider(config);
        leader = new KingdomBuilder(game).withName("leader").withUnit(UnitName.cavalry, 100).build();
        joiner = new KingdomBuilder(game).withName("joiner").withUnit(UnitName.cavalry, 100).build();
        stranger = new KingdomBuilder(game).withName("stranger").withUnit(UnitName.cavalry, 100).build();
        target = new KingdomBuilder(game).withName("target").build();
        game.addKingdom(leader);
        game.addKingdom(joiner);
        game.addKingdom(stranger);
        game.addKingdom(target);
        var alliance = new AllianceEntity("Pact", "leader");
        alliance.addKingdom(leader);
        alliance.addKingdom(joiner);
    }

    private static UnitsMapDto cavalry(int count)
    {
        var units = new UnitsMapDto();
        units.setCount(UnitName.cavalry, count);
        return units;
    }

    private KingdomOngoingAttackEntity march(KingdomEntity kingdom, KingdomOngoingAttackEntity lead)
    {
        var sent = new KingdomMilitaryAction(kingdom, config).sendAttack(new SendAttackDto("target", AttackType.land, cavalry(20)), target, lead);
        assertTrue(sent.success(), sent.message());
        return kingdom.getOngoingAttacks().get(kingdom.getOngoingAttacks().size() - 1);
    }

    private void passTurns(KingdomEntity kingdom, int turns)
    {
        for (int turn = 0; turn < turns; turn++)
        {
            kingdom.getResources().addCount(ResourceName.turns, 1);
            new KingdomTurnAction(kingdom, game.getKingdomInteractor(), config, details).passTurn(0);
        }
    }

    private long battlesBy(KingdomEntity kingdom)
    {
        return game.getWarReportRepository().getBattleReports(kingdom.getName(), 20).stream().map(BattleReportDto::attacker).filter(kingdom.getName()::equals).count();
    }

    @Test
    void aJoinedArmy_shouldWaitAtTheBorderUntilTheLeadLands()
    {
        var lead = march(leader, null);
        march(joiner, lead);

        passTurns(joiner, config.attack().turnsToArrive() + 3);

        assertThat(joiner.getOngoingAttacks()).singleElement().satisfies(attack -> assertEquals(0, attack.toDto().turnsLeft));
        assertEquals(0, battlesBy(joiner));

        passTurns(leader, config.attack().turnsToArrive());

        assertThat(leader.getOngoingAttacks()).isEmpty();
        assertThat(joiner.getOngoingAttacks()).isEmpty();
        assertEquals(1, battlesBy(leader));
        assertEquals(1, battlesBy(joiner));
        assertThat(game.getNotificationSystem().getNotifications("leader")).extracting(NotificationDto::message)
                .anyMatch(message -> message.startsWith("joiner struck target right after our army"));
        assertEquals(100, joiner.getUnits().getAvailableCount(UnitName.cavalry) + joiner.getUnits().getMobileCount(UnitName.cavalry)
                + game.getWarReportRepository().getBattleReports("joiner", 1).get(0).attackerLosses().getCount(UnitName.cavalry));
    }

    @Test
    void aJoinedArmyThatArrivesAfterTheLead_shouldLandWhenItArrives()
    {
        var lead = march(leader, null);
        passTurns(leader, config.attack().turnsToArrive() - 1);
        march(joiner, lead);
        passTurns(leader, 1);
        assertEquals(1, battlesBy(leader));

        passTurns(joiner, config.attack().turnsToArrive());

        assertThat(joiner.getOngoingAttacks()).isEmpty();
        assertEquals(1, battlesBy(joiner));
    }

    @Test
    void aJoinedArmyWhoseLeadTurnedBack_shouldLandOnItsOwnNextTurn()
    {
        var lead = march(leader, null);
        march(joiner, lead);
        passTurns(joiner, config.attack().turnsToArrive());
        assertThat(joiner.getOngoingAttacks()).hasSize(1);

        new KingdomMilitaryAction(leader, config).withdrawAttack(lead);
        passTurns(joiner, 1);

        assertThat(joiner.getOngoingAttacks()).isEmpty();
        assertEquals(1, battlesBy(joiner));
    }

    @Test
    void aJoinedArmyWhoseLeadIsSlow_shouldFightAloneOnceItHasWaitedLongEnough()
    {
        var lead = march(leader, null);
        march(joiner, lead);

        passTurns(joiner, config.attack().turnsToArrive() + config.attack().turnsToWaitAtBorder() - 1);
        assertThat(joiner.getOngoingAttacks()).singleElement().satisfies(attack -> assertEquals(config.attack().turnsToWaitAtBorder() - 1, attack.toDto().turnsAtBorder));
        assertEquals(0, battlesBy(joiner));

        passTurns(joiner, 1);

        assertThat(joiner.getOngoingAttacks()).isEmpty();
        assertEquals(1, battlesBy(joiner));
        assertThat(leader.getOngoingAttacks()).hasSize(1);
    }

    @Test
    void onlyAnAllysStrikeOnTheSameTarget_canBeJoined()
    {
        var lead = march(leader, null);
        var other = new KingdomBuilder(game).withName("other").build();
        game.addKingdom(other);

        var byStranger = new KingdomMilitaryAction(stranger, config).sendAttack(new SendAttackDto("target", AttackType.land, cavalry(20)), target, lead);
        var elsewhere = new KingdomMilitaryAction(joiner, config).sendAttack(new SendAttackDto("other", AttackType.land, cavalry(20)), other, lead);
        var ownStrike = new KingdomMilitaryAction(leader, config).sendAttack(new SendAttackDto("target", AttackType.land, cavalry(20)), target, lead);

        assertFalse(byStranger.success());
        assertFalse(elsewhere.success());
        assertFalse(ownStrike.success());
        assertThat(stranger.getOngoingAttacks()).isEmpty();
        assertThat(joiner.getOngoingAttacks()).isEmpty();
        assertEquals(100, stranger.getUnits().getAvailableCount(UnitName.cavalry));
    }
}
