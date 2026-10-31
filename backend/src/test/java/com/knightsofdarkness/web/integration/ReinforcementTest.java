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
import com.knightsofdarkness.web.common.kingdom.SendAttackDto;
import com.knightsofdarkness.web.common.kingdom.SendReinforcementsDto;
import com.knightsofdarkness.web.common.kingdom.UnitName;
import com.knightsofdarkness.web.common.kingdom.UnitsMapDto;
import com.knightsofdarkness.web.common.messaging.NotificationDto;
import com.knightsofdarkness.web.common.messaging.NotificationKind;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.kingdom.model.KingdomDetailsProvider;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;
import com.knightsofdarkness.web.kingdom.model.KingdomInteractor;
import com.knightsofdarkness.web.kingdom.model.KingdomMilitaryAction;
import com.knightsofdarkness.web.kingdom.model.KingdomTurnAction;
import com.knightsofdarkness.web.legacy.TestGame;
import com.knightsofdarkness.web.utils.KingdomBuilder;

class ReinforcementTest
{
    private Game game;
    private GameConfig config;
    private KingdomEntity guardian;
    private KingdomEntity farmer;
    private KingdomEntity raider;
    private KingdomDetailsProvider details;

    @BeforeEach
    void setUp()
    {
        game = new TestGame().get();
        config = game.getConfig();
        details = new KingdomDetailsProvider(config);
        guardian = new KingdomBuilder(game).withName("guardian").withUnit(UnitName.infantry, 100).build();
        farmer = new KingdomBuilder(game).withName("farmer").build();
        raider = new KingdomBuilder(game).withName("raider").withUnit(UnitName.cavalry, 100).build();
        game.addKingdom(guardian);
        game.addKingdom(farmer);
        game.addKingdom(raider);
        var alliance = new AllianceEntity("Shield", "guardian");
        alliance.addKingdom(guardian);
        alliance.addKingdom(farmer);
    }

    private static UnitsMapDto units(UnitName unit, int count)
    {
        var units = new UnitsMapDto();
        units.setCount(unit, count);
        return units;
    }

    private void passTurns(KingdomEntity kingdom, int turns)
    {
        for (int turn = 0; turn < turns; turn++)
        {
            kingdom.getResources().addCount(com.knightsofdarkness.web.common.kingdom.ResourceName.turns, 1);
            new KingdomTurnAction(kingdom, game.getKingdomInteractor(), config, details).passTurn(0);
        }
    }

    private void standGuard(int infantry)
    {
        var sent = new KingdomMilitaryAction(guardian, config).sendReinforcements(new SendReinforcementsDto("farmer", units(UnitName.infantry, infantry)), farmer);
        assertTrue(sent.success(), sent.message());
        passTurns(guardian, config.common().turnsToReinforce());
    }

    @Test
    void reinforcements_shouldLeaveHomeAndDefendTheAllyOnceArrived()
    {
        var defenseBefore = details.getDefensePower(farmer);
        var sent = new KingdomMilitaryAction(guardian, config).sendReinforcements(new SendReinforcementsDto("farmer", units(UnitName.infantry, 40)), farmer);

        assertTrue(sent.success(), sent.message());
        assertEquals(60, guardian.getUnits().getAvailableCount(UnitName.infantry));
        assertEquals(40, guardian.getUnits().getMobileCount(UnitName.infantry));
        assertEquals(defenseBefore, details.getDefensePower(farmer), 0.001, "soldiers on the road do not defend yet");

        passTurns(guardian, config.common().turnsToReinforce());

        assertTrue(details.getDefensePower(farmer) > defenseBefore);
        assertThat(farmer.getGarrisons()).singleElement().satisfies(garrison -> assertTrue(garrison.isStanding()));
        assertThat(game.getNotificationSystem().getNotifications("farmer")).extracting(NotificationDto::message).anyMatch(message -> message.contains("guardian"));
    }

    @Test
    void reinforcements_shouldJoinTheGarrisonAlreadyStandingThere()
    {
        standGuard(30);
        standGuard(20);

        assertThat(farmer.getGarrisons()).singleElement().satisfies(garrison -> assertEquals(50, garrison.getUnits().getCount(UnitName.infantry)));
        assertThat(guardian.getReinforcements()).hasSize(1);
        assertEquals(50, guardian.getUnits().getMobileCount(UnitName.infantry));
    }

    @Test
    void reinforcements_shouldOnlyGoToAllies()
    {
        var sent = new KingdomMilitaryAction(guardian, config).sendReinforcements(new SendReinforcementsDto("raider", units(UnitName.infantry, 10)), raider);

        assertFalse(sent.success());
        assertEquals(100, guardian.getUnits().getAvailableCount(UnitName.infantry));
    }

    @Test
    void garrison_shouldFightBesideTheHostAndShareTheLosses()
    {
        standGuard(100);
        var attack = new KingdomMilitaryAction(raider, config).sendAttack(new SendAttackDto("farmer", AttackType.land, units(UnitName.cavalry, 100)), farmer);
        assertTrue(attack.success(), attack.message());

        ((KingdomInteractor) game.getKingdomInteractor()).resolveAttack(raider, raider.getOngoingAttacks().get(0));

        var report = game.getWarReportRepository().getBattleReports("farmer", 1).get(0);
        // 100 cavalry land 100 melee blows on infantry that takes 3 each
        assertEquals(33, report.alliedLosses().getCount(UnitName.infantry));
        assertEquals(100 - 33, farmer.getGarrisons().get(0).getUnits().getCount(UnitName.infantry));
        assertEquals(100 - 33, guardian.getUnits().getMobileCount(UnitName.infantry));
        // 25 cavalry fall to the infantry; the 75 left attack with 225 against 67 infantry and 5 guards behind walls, about 164
        assertEquals(25, report.attackerLosses().getCount(UnitName.cavalry));
        assertEquals((67 * 2 + 5) * (1 + 4 * config.attack().towerDefenseBonusPerTower() + config.attack().castleDefenseBonusPerCastle()), report.defensePower(), 0.001);
        assertThat(game.getNotificationSystem().getNotifications("guardian")).extracting(NotificationDto::kind).contains(NotificationKind.breached);
    }

    @Test
    void recall_shouldBringTheSoldiersHomeAtOnce()
    {
        standGuard(40);
        var garrison = guardian.getReinforcements().get(0);

        new KingdomMilitaryAction(guardian, config).recallReinforcements(garrison);

        assertEquals(100, guardian.getUnits().getAvailableCount(UnitName.infantry));
        assertEquals(0, guardian.getUnits().getMobileCount(UnitName.infantry));
        assertThat(farmer.getGarrisons()).isEmpty();
        assertThat(guardian.getReinforcements()).isEmpty();
    }

    @Test
    void leavingTheAlliance_shouldSendGarrisonsHomeBothWays()
    {
        standGuard(40);

        farmer.removeAlliance();

        assertEquals(100, guardian.getUnits().getAvailableCount(UnitName.infantry));
        assertThat(farmer.getGarrisons()).isEmpty();
        assertThat(guardian.getReinforcements()).isEmpty();
    }
}
