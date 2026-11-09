package com.knightsofdarkness.web.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.web.Game;
import com.knightsofdarkness.web.alliance.model.AllianceEntity;
import com.knightsofdarkness.web.common.alliance.MonumentContributionDto;
import com.knightsofdarkness.web.common.kingdom.AttackType;
import com.knightsofdarkness.web.common.kingdom.BuildingName;
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
import com.knightsofdarkness.web.monument.model.MonumentEntity;
import com.knightsofdarkness.web.utils.KingdomBuilder;
import com.knightsofdarkness.web.utils.MonumentFront;

class MonumentAssaultTest {
    private final MonumentFront front = new MonumentFront();
    private Game game;
    private GameConfig config;
    private KingdomEntity mason;
    private KingdomEntity seat;
    private KingdomEntity raider;
    private MonumentEntity monument;

    @BeforeEach
    void setUp()
    {
        game = new TestGame(front).get();
        config = game.getConfig();
        mason = new KingdomBuilder(game).withName("mason").withUnit(UnitName.infantry, 100).build();
        seat = new KingdomBuilder(game).withName("seat").build();
        raider = new KingdomBuilder(game).withName("raider").withUnit(UnitName.cavalry, 2000).withUnit(UnitName.bowman, 100).build();
        game.addKingdom(mason);
        game.addKingdom(seat);
        game.addKingdom(raider);
        var order = new AllianceEntity("Order", "seat");
        order.addKingdom(seat);
        order.addKingdom(mason);
        monument = new MonumentEntity("Order", "seat");
        front.monuments.add(monument);
        raiseStages(4);
    }

    private void raiseStages(int count)
    {
        var cost = config.monument().stageCost();
        for (int paid = 0; paid * 2_000_000_000L < cost.gold() * count; paid++)
        {
            monument.pay(new MonumentContributionDto(2_000_000_000, 0, 0, 0));
        }
        monument.pay(new MonumentContributionDto(0, (int) cost.iron() * count, (int) cost.tools() * count, 0));
        assertThat(monument.raiseStages(config.monument())).hasSize(count);
    }

    private double stagesStanding()
    {
        return monument.toDto(config.monument()).stages();
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
            kingdom.getResources().addCount(ResourceName.turns, 1);
            new KingdomTurnAction(kingdom, game.getKingdomInteractor(), config, new KingdomDetailsProvider(config)).passTurn(0);
        }
    }

    private void standGuardAtTheMonument(int infantry)
    {
        var sent = new KingdomMilitaryAction(mason, config).sendToMonument(units(UnitName.infantry, infantry), monument);
        assertTrue(sent.success(), sent.message());
        passTurns(mason, config.common().turnsToReinforce());
    }

    private KingdomOngoingAttackEntity marchOnTheMonument(KingdomEntity attacker, UnitsMapDto army)
    {
        var sent = new KingdomMilitaryAction(attacker, config).sendAttack(new SendAttackDto("seat", AttackType.monument, army), seat);
        assertTrue(sent.success(), sent.message());
        return attacker.getOngoingAttacks().getLast();
    }

    private BattleReportDto assault(KingdomEntity attacker, UnitsMapDto army)
    {
        game.getKingdomInteractor().resolveAttack(attacker, marchOnTheMonument(attacker, army));
        return game.getWarReportRepository().getBattleReports(attacker.getName(), 1).getFirst();
    }

    @Test
    void soldiersSentToTheMonument_shouldLeaveHomeAndStandGuardOnceArrived()
    {
        var sent = new KingdomMilitaryAction(mason, config).sendToMonument(units(UnitName.infantry, 40), monument);

        assertTrue(sent.success(), sent.message());
        assertEquals(60, mason.getUnits().getAvailableCount(UnitName.infantry));
        assertEquals(40, mason.getUnits().getMobileCount(UnitName.infantry));
        assertThat(monument.getStandingGarrison()).isEmpty();
        assertEquals(0, monument.toDto(config.monument()).garrison(), "soldiers on the road are not on guard yet");
        assertThat(mason.getReinforcements()).singleElement().satisfies(guard -> assertTrue(guard.toDto().atMonument()));

        passTurns(mason, config.common().turnsToReinforce());

        assertThat(monument.getStandingGarrison()).hasSize(1);
        assertEquals(40, monument.toDto(config.monument()).garrison());
        assertThat(game.getNotificationSystem().getNotifications("mason")).extracting(NotificationDto::message).anyMatch(message -> message.contains("stand guard at the monument at seat"));
    }

    @Test
    void soldiersArrivingAtTheMonument_shouldJoinTheGarrisonAlreadyThere()
    {
        standGuardAtTheMonument(30);
        standGuardAtTheMonument(20);

        assertThat(monument.getGarrison()).singleElement().satisfies(garrison -> assertEquals(50, garrison.getUnits().getCount(UnitName.infantry)));
        assertThat(mason.getReinforcements()).hasSize(1);
        assertEquals(50, mason.getUnits().getMobileCount(UnitName.infantry));
    }

    @Test
    void soldiersCalledHome_shouldLeaveTheMonumentAtOnce()
    {
        standGuardAtTheMonument(40);

        new KingdomMilitaryAction(mason, config).recallReinforcements(mason.getReinforcements().getFirst());

        assertThat(monument.getGarrison()).isEmpty();
        assertThat(mason.getReinforcements()).isEmpty();
        assertEquals(100, mason.getUnits().getAvailableCount(UnitName.infantry));
    }

    @Test
    void aMemberLeavingTheAlliance_shouldTakeTheirSoldiersHomeFromTheMonument()
    {
        standGuardAtTheMonument(40);

        mason.removeAlliance();

        assertThat(monument.getGarrison()).isEmpty();
        assertEquals(100, mason.getUnits().getAvailableCount(UnitName.infantry));
    }

    @Test
    void anUnguardedMonument_shouldLoseTheFullShareWhateverTheSeatHasAtHome()
    {
        seat.getUnits().addCount(UnitName.infantry, 5000);
        seat.getUnits().addCount(UnitName.guard, 5000);
        seat.getBuildings().setCount(BuildingName.tower, 100);
        seat.getBuildings().setCount(BuildingName.castle, 100);

        var report = assault(raider, units(UnitName.cavalry, 100));

        assertTrue(report.attackerWon());
        assertEquals(0, report.defensePower(), 0.001);
        assertEquals(1, report.stagesRazed(), 0.001, "a quarter of the four stages standing");
        assertEquals(3, stagesStanding(), 0.001);
        assertEquals(100, report.unitsSent().getCount(UnitName.cavalry) - report.attackerLosses().getCount(UnitName.cavalry), "nobody stood there to strike back");
        assertEquals(5000, seat.getUnits().getAvailableCount(UnitName.infantry), "the seat's own army takes no part");
        assertEquals(AttackType.monument, report.attackType());
        assertEquals("seat", report.defender());
        assertEquals(0, report.landCaptured());
        assertThat(front.heard).containsExactly("raider won at Order");
    }

    @Test
    void aWonAssault_shouldTakeNoLandAndNoStores()
    {
        var land = seat.getResources().getCount(ResourceName.land);
        var gold = seat.getResources().getCount(ResourceName.gold);

        var report = assault(raider, units(UnitName.cavalry, 100));

        assertEquals(land, seat.getResources().getCount(ResourceName.land));
        assertEquals(gold, seat.getResources().getCount(ResourceName.gold));
        assertThat(report.plunder()).isEmpty();
        assertEquals(0, seat.getProtectionTicks(), "a monument's seat earns no protection from it");
    }

    @Test
    void theGarrison_shouldFightAlone_andAWinShouldBeScaledByDominance()
    {
        standGuardAtTheMonument(100);
        seat.getBuildings().setCount(BuildingName.castle, 100);

        var report = assault(raider, units(UnitName.cavalry, 100));

        // 100 blows on infantry that takes 3 each, no castle's walls; 100 blows back on cavalry that takes 4
        assertEquals(33, report.defenderLosses().getCount(UnitName.infantry));
        assertEquals(33, report.alliedLosses().getCount(UnitName.infantry));
        assertEquals(25, report.attackerLosses().getCount(UnitName.cavalry));
        assertEquals(67 * 2, report.defensePower(), 0.001);
        assertEquals(75 * 3, report.attackPower(), 0.001);
        var dominance = 225.0 / 134 - 1;
        assertEquals(4 * config.monument().maxRazeRatio() * dominance, report.stagesRazed(), 0.01);
        assertEquals(4 - report.stagesRazed(), stagesStanding(), 0.01);
        assertEquals(67, mason.getUnits().getMobileCount(UnitName.infantry), "the fallen are gone from the sender's soldiers away");
        assertEquals(67, monument.getStandingGarrison().getFirst().getUnits().getCount(UnitName.infantry));
    }

    @Test
    void anAssaultThatFails_shouldLeaveTheMonumentStanding()
    {
        standGuardAtTheMonument(100);

        var report = assault(raider, units(UnitName.cavalry, 20));

        assertFalse(report.attackerWon());
        assertEquals(0, report.stagesRazed(), 0.001);
        assertEquals(4, stagesStanding(), 0.001);
        assertThat(front.heard).containsExactly("raider repelled at Order");
        assertThat(game.getNotificationSystem().getNotifications("raider")).extracting(NotificationDto::message).anyMatch(message -> message.contains("repelled at the monument of Order"));
    }

    @Test
    void soldiersStillOnTheRoad_shouldNotDefendTheMonument()
    {
        var sent = new KingdomMilitaryAction(mason, config).sendToMonument(units(UnitName.infantry, 100), monument);
        assertTrue(sent.success(), sent.message());

        var report = assault(raider, units(UnitName.cavalry, 10));

        assertEquals(0, report.defensePower(), 0.001);
        assertEquals(100, mason.getUnits().getMobileCount(UnitName.infantry));
    }

    @Test
    void anEmptiedGarrison_shouldBeGone()
    {
        standGuardAtTheMonument(10);

        assault(raider, units(UnitName.cavalry, 1000));

        assertThat(monument.getGarrison()).isEmpty();
        assertThat(mason.getReinforcements()).isEmpty();
        assertEquals(0, mason.getUnits().getMobileCount(UnitName.infantry));
    }

    @Test
    void aMonument_shouldHaveNoProtectionAndNoSize()
    {
        seat.setProtectionTicks(100);
        seat.getResources().subtractCount(ResourceName.land, seat.getResources().getCount(ResourceName.land) - 1);

        var first = assault(raider, units(UnitName.cavalry, 100));
        var second = assault(raider, units(UnitName.cavalry, 100));

        assertTrue(first.attackerWon());
        assertTrue(second.attackerWon());
        assertEquals(4 * 0.75 * 0.75, stagesStanding(), 0.001);
    }

    @Test
    void aKingdom_shouldNotAssaultItsOwnAlliancesMonument()
    {
        var sent = new KingdomMilitaryAction(mason, config).sendAttack(new SendAttackDto("seat", AttackType.monument, units(UnitName.infantry, 10)), seat);

        assertFalse(sent.success());
        assertThat(sent.message()).contains("your own alliance's monument");
    }

    @Test
    void anArmyWhoseTargetHasFallen_shouldComeHome()
    {
        var attack = marchOnTheMonument(raider, units(UnitName.cavalry, 100));
        front.monuments.clear();

        game.getKingdomInteractor().resolveAttack(raider, attack);

        assertEquals(2000, raider.getUnits().getAvailableCount(UnitName.cavalry));
        assertEquals(0, raider.getUnits().getMobileCount(UnitName.cavalry));
        assertThat(game.getWarReportRepository().getBattleReports("raider", 1)).isEmpty();
        assertThat(game.getNotificationSystem().getNotifications("raider")).extracting(NotificationDto::message).anyMatch(message -> message.contains("no enemy monument"));
    }

    @Test
    void anArmyWhoseOwnerJoinedTheAlliance_shouldComeHomeWithoutAFight()
    {
        var attack = marchOnTheMonument(raider, units(UnitName.cavalry, 100));
        seat.getAlliance().orElseThrow().addKingdom(raider);

        game.getKingdomInteractor().resolveAttack(raider, attack);

        assertEquals(4, stagesStanding(), 0.001);
        assertEquals(2000, raider.getUnits().getAvailableCount(UnitName.cavalry));
    }

    @Test
    void joinedArmies_shouldAssaultRightAfterTheirLead()
    {
        var ally = new KingdomBuilder(game).withName("ally").withUnit(UnitName.cavalry, 100).build();
        game.addKingdom(ally);
        var rivals = new AllianceEntity("Rivals", "raider");
        rivals.addKingdom(raider);
        rivals.addKingdom(ally);

        var lead = marchOnTheMonument(raider, units(UnitName.cavalry, 100));
        var joined = new KingdomMilitaryAction(ally, config).sendAttack(new SendAttackDto("seat", AttackType.monument, units(UnitName.cavalry, 100)), seat, lead);
        assertTrue(joined.success(), joined.message());

        passTurns(ally, config.attack().turnsToArrive());
        assertThat(game.getWarReportRepository().getBattleReports("ally", 1)).as("it waits at the border").isEmpty();
        passTurns(raider, config.attack().turnsToArrive());

        assertThat(game.getWarReportRepository().getBattleReports("ally", 1)).singleElement().satisfies(report -> assertEquals(AttackType.monument, report.attackType()));
        assertEquals(4 * 0.75 * 0.75, stagesStanding(), 0.001);
    }

    @Test
    void aStrikeOnAKingdomAndAnAssaultOnItsMonument_shouldNotJoin()
    {
        var ally = new KingdomBuilder(game).withName("ally").withUnit(UnitName.cavalry, 100).build();
        game.addKingdom(ally);
        var rivals = new AllianceEntity("Rivals", "raider");
        rivals.addKingdom(raider);
        rivals.addKingdom(ally);
        var assault = marchOnTheMonument(raider, units(UnitName.cavalry, 100));
        var strike = new KingdomMilitaryAction(raider, config).sendAttack(new SendAttackDto("seat", AttackType.land, units(UnitName.cavalry, 100)), seat);
        assertTrue(strike.success(), strike.message());
        var strikeLead = raider.getOngoingAttacks().getLast();

        var onTheKingdom = new KingdomMilitaryAction(ally, config).sendAttack(new SendAttackDto("seat", AttackType.land, units(UnitName.cavalry, 10)), seat, assault);
        var onTheMonument = new KingdomMilitaryAction(ally, config).sendAttack(new SendAttackDto("seat", AttackType.monument, units(UnitName.cavalry, 10)), seat, strikeLead);

        assertFalse(onTheKingdom.success());
        assertThat(onTheKingdom.message()).contains("marches on the monument");
        assertFalse(onTheMonument.success());
        assertThat(onTheMonument.message()).contains("marches on the kingdom");
    }
}
