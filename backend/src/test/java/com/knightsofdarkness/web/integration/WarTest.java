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
import com.knightsofdarkness.web.common.kingdom.BuildingName;
import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.kingdom.SendAttackDto;
import com.knightsofdarkness.web.common.kingdom.SpecialBuildingType;
import com.knightsofdarkness.web.common.kingdom.UnitName;
import com.knightsofdarkness.web.common.kingdom.UnitsMapDto;
import com.knightsofdarkness.web.common.messaging.NotificationDto;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.kingdom.model.KingdomDetailsProvider;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;
import com.knightsofdarkness.web.kingdom.model.KingdomInteractor;
import com.knightsofdarkness.web.kingdom.model.KingdomMilitaryAction;
import com.knightsofdarkness.web.kingdom.model.KingdomSpecialBuildingAction;
import com.knightsofdarkness.web.kingdom.model.KingdomSpyAction;
import com.knightsofdarkness.web.legacy.TestGame;
import com.knightsofdarkness.web.utils.KingdomBuilder;

class WarTest {
    private Game game;
    private GameConfig gameConfig;
    private KingdomEntity attacker;
    private KingdomEntity defender;

    @BeforeEach
    void setUp()
    {
        game = new TestGame().get();
        gameConfig = game.getConfig();
        attacker = new KingdomBuilder(game).withName("attacker").withUnit(UnitName.cavalry, 100).withUnit(UnitName.spy, 50).build();
        defender = new KingdomBuilder(game).withName("defender").build();
        game.addKingdom(attacker);
        game.addKingdom(defender);
    }

    private static UnitsMapDto cavalry(int count)
    {
        var units = new UnitsMapDto();
        units.setCount(UnitName.cavalry, count);
        return units;
    }

    private void sendAndResolve(AttackType type, UnitsMapDto units)
    {
        var result = new KingdomMilitaryAction(attacker, gameConfig).sendAttack(new SendAttackDto(defender.getName(), type, units), defender);
        assertTrue(result.success(), result.message());
        ((KingdomInteractor) game.getKingdomInteractor()).resolveAttack(attacker, attacker.getOngoingAttacks().get(0));
    }

    @Test
    void attackingOwnKingdom_shouldFail()
    {
        var result = new KingdomMilitaryAction(attacker, gameConfig).sendAttack(new SendAttackDto(attacker.getName(), AttackType.land, cavalry(10)), attacker);

        assertFalse(result.success());
        assertEquals(100, attacker.getUnits().getAvailableCount(UnitName.cavalry));
    }

    @Test
    void attackingAllianceMember_shouldFail()
    {
        var alliance = new AllianceEntity("Order", attacker.getName());
        alliance.addKingdom(attacker);
        alliance.addKingdom(defender);

        var result = new KingdomMilitaryAction(attacker, gameConfig).sendAttack(new SendAttackDto(defender.getName(), AttackType.land, cavalry(10)), defender);

        assertFalse(result.success());
    }

    @Test
    void attackingMuchSmallerKingdom_shouldFail()
    {
        defender.getResources().setCount(ResourceName.land, 49);

        var result = new KingdomMilitaryAction(attacker, gameConfig).sendAttack(new SendAttackDto(defender.getName(), AttackType.land, cavalry(10)), defender);

        assertFalse(result.success());
    }

    @Test
    void attackWithNegativeUnits_shouldFail()
    {
        var units = cavalry(10);
        units.setCount(UnitName.infantry, -5);

        var result = new KingdomMilitaryAction(attacker, gameConfig).sendAttack(new SendAttackDto(defender.getName(), AttackType.land, units), defender);

        assertFalse(result.success());
        assertEquals(0, attacker.getUnits().getAvailableCount(UnitName.infantry));
    }

    @Test
    void attackWithCivilians_shouldOnlySendMilitary()
    {
        var units = cavalry(10);
        units.setCount(UnitName.goldMiner, 10);

        new KingdomMilitaryAction(attacker, gameConfig).sendAttack(new SendAttackDto(defender.getName(), AttackType.land, units), defender);

        assertEquals(0, attacker.getOngoingAttacks().get(0).toDto().units.getCount(UnitName.goldMiner));
        assertEquals(0, attacker.getUnits().getMobileCount(UnitName.goldMiner));
    }

    @Test
    void wonLandAttack_shouldCaptureUnusedLand()
    {
        // start kingdom has 100 land and 59 buildings, 5 guards behind 4 towers and a castle
        sendAndResolve(AttackType.land, cavalry(100));

        assertEquals(110, attacker.getResources().getCount(ResourceName.land));
        assertEquals(90, defender.getResources().getCount(ResourceName.land));
        assertEquals(59, defender.getBuildings().countAll());
        assertEquals(100, attacker.getUnits().getAvailableCount(UnitName.cavalry));
        assertEquals(0, attacker.getUnits().getMobileCount(UnitName.cavalry));
    }

    @Test
    void wonLandAttack_onFullyBuiltKingdom_shouldRazeBuildings()
    {
        defender.getResources().setCount(ResourceName.land, 59);

        sendAndResolve(AttackType.land, cavalry(100));

        assertEquals(54, defender.getResources().getCount(ResourceName.land));
        assertEquals(54, defender.getBuildings().countAll());
        assertEquals(105, attacker.getResources().getCount(ResourceName.land));
    }

    @Test
    void wonEconomyAttack_shouldPlunderResources()
    {
        var defenderGold = defender.getResources().getCount(ResourceName.gold);
        var attackerGold = attacker.getResources().getCount(ResourceName.gold);

        sendAndResolve(AttackType.economy, cavalry(100));

        var plundered = (int) Math.floor(defenderGold * gameConfig.attack().maxPlunderRatio());
        assertEquals(defenderGold - plundered, defender.getResources().getCount(ResourceName.gold));
        assertEquals(attackerGold + plundered, attacker.getResources().getCount(ResourceName.gold));
        assertEquals(100, defender.getResources().getCount(ResourceName.land));
    }

    @Test
    void warehouses_shouldProtectAgainstPlunder()
    {
        var specialBuildingAction = new KingdomSpecialBuildingAction(defender, gameConfig);
        var warehouse = specialBuildingAction.startSpecialBuilding(SpecialBuildingType.warehouse).orElseThrow();
        for (int level = 0; level < 5; level++)
        {
            defender.getResources().setCount(ResourceName.buildingPoints, 1000000);
            specialBuildingAction.buildSpecialBuilding(warehouse, 1000000);
        }
        var defenderGold = defender.getResources().getCount(ResourceName.gold);

        sendAndResolve(AttackType.economy, cavalry(100));

        var protection = 5 * gameConfig.attack().warehousePlunderProtectionPerLevel();
        var plundered = (int) Math.floor(defenderGold * gameConfig.attack().maxPlunderRatio() * (1 - protection));
        assertEquals(defenderGold - plundered, defender.getResources().getCount(ResourceName.gold));
    }

    @Test
    void lostAttack_shouldNotCaptureAnything()
    {
        // melee is fought type against type, so defending cavalry is what kills attacking cavalry
        defender.getUnits().setCount(UnitName.cavalry, 1000);

        sendAndResolve(AttackType.land, cavalry(10));

        assertEquals(100, attacker.getResources().getCount(ResourceName.land));
        assertEquals(100, defender.getResources().getCount(ResourceName.land));
        var report = game.getWarReportRepository().getBattleReports(attacker.getName(), 10).get(0);
        assertFalse(report.attackerWon());
        assertThat(report.attackerLosses().getCount(UnitName.cavalry)).isPositive();
    }

    @Test
    void resolvedAttack_shouldReportToBothSides()
    {
        sendAndResolve(AttackType.land, cavalry(100));

        var attackerReports = game.getWarReportRepository().getBattleReports(attacker.getName(), 10);
        var defenderReports = game.getWarReportRepository().getBattleReports(defender.getName(), 10);
        assertEquals(1, attackerReports.size());
        assertEquals(attackerReports, defenderReports);
        var report = attackerReports.get(0);
        assertTrue(report.attackerWon());
        assertEquals(10, report.landCaptured());
        assertEquals(100, report.unitsSent().getCount(UnitName.cavalry));

        var defenderNotifications = game.getNotificationSystem().getNotifications(defender.getName());
        assertThat(defenderNotifications).extracting(NotificationDto::message).anyMatch(message -> message.contains(attacker.getName()));
        assertThat(game.getNotificationSystem().getNotifications(attacker.getName())).extracting(NotificationDto::message).anyMatch(message -> message.startsWith("Victory"));
    }

    @Test
    void towersAndCastles_shouldIncreaseDefense()
    {
        var detailsProvider = new KingdomDetailsProvider(gameConfig);
        defender.getBuildings().setCount(BuildingName.tower, 0);
        defender.getBuildings().setCount(BuildingName.castle, 0);
        var bareDefense = detailsProvider.getDefensePower(defender);

        defender.getBuildings().setCount(BuildingName.tower, 10);
        defender.getBuildings().setCount(BuildingName.castle, 1);

        assertEquals(bareDefense * 1.3, detailsProvider.getDefensePower(defender), 0.0001);
    }

    @Test
    void spiesOutnumberingCounterIntelligence_shouldBringIntel()
    {
        var result = new KingdomSpyAction(attacker, gameConfig).sendSpies(defender, 10);

        assertTrue(result.success());
        var report = result.report().orElseThrow();
        assertTrue(report.success());
        assertEquals(defender.getResources().getCount(ResourceName.gold), report.resources().get(ResourceName.gold));
        assertEquals(5, report.units().get(UnitName.guard));
        assertEquals(50, attacker.getUnits().getAvailableCount(UnitName.spy));
    }

    @Test
    void spiesCaughtByCounterIntelligence_shouldBeLost()
    {
        defender.getUnits().setCount(UnitName.spy, 10);

        var result = new KingdomSpyAction(attacker, gameConfig).sendSpies(defender, 20);

        var report = result.report().orElseThrow();
        assertFalse(report.success());
        assertEquals(10, report.spiesLost());
        assertTrue(report.resources().isEmpty());
        assertEquals(40, attacker.getUnits().getAvailableCount(UnitName.spy));
    }

    @Test
    void sendingMoreSpiesThanAvailable_shouldFail()
    {
        var result = new KingdomSpyAction(attacker, gameConfig).sendSpies(defender, 51);

        assertFalse(result.success());
        assertEquals(50, attacker.getUnits().getAvailableCount(UnitName.spy));
    }
}
