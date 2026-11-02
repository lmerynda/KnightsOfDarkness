package com.knightsofdarkness.web.kingdom.model;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knightsofdarkness.web.common.kingdom.AttackType;
import com.knightsofdarkness.web.common.kingdom.BuildingName;
import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.kingdom.SpecialBuildingType;
import com.knightsofdarkness.web.common.kingdom.UnitName;
import com.knightsofdarkness.web.common.kingdom.UnitsMapDto;
import com.knightsofdarkness.web.common.market.MarketResource;
import com.knightsofdarkness.web.common.messaging.NotificationKind;
import com.knightsofdarkness.web.common.war.BattleReportDto;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.kingdom.IKingdomInteractor;
import com.knightsofdarkness.web.kingdom.IKingdomRepository;
import com.knightsofdarkness.web.messaging.INotificationSystem;
import com.knightsofdarkness.web.utils.Id;
import com.knightsofdarkness.web.utils.Utils;
import com.knightsofdarkness.web.war.IWarReportRepository;

public class KingdomInteractor implements IKingdomInteractor
{
    private static final Logger log = LoggerFactory.getLogger(KingdomInteractor.class);
    private static final Map<UnitName, Integer> meleeBlowsToFall = Map.of(UnitName.bowman, 2, UnitName.infantry, 3, UnitName.cavalry, 4);
    private static final List<ResourceName> plunderableResources = List.of(ResourceName.gold, ResourceName.food, ResourceName.iron, ResourceName.tools, ResourceName.weapons);
    private final IKingdomRepository kingdomRepository;
    private final GameConfig gameConfig;
    private final INotificationSystem notificationSystem;
    private final IWarReportRepository warReportRepository;
    private final KingdomDetailsProvider detailsProvider;

    public KingdomInteractor(IKingdomRepository kingdomRepository, GameConfig gameConfig, INotificationSystem notificationSystem, IWarReportRepository warReportRepository)
    {
        this.kingdomRepository = kingdomRepository;
        this.gameConfig = gameConfig;
        this.notificationSystem = notificationSystem;
        this.warReportRepository = warReportRepository;
        this.detailsProvider = new KingdomDetailsProvider(gameConfig);
    }

    @Override
    public void transferResources(KingdomEntity from, String to, MarketResource resource, int amount)
    {
        var toKingdom = kingdomRepository.getKingdomByName(to);
        if (toKingdom.isEmpty())
        {
            log.warn("[KingdomInteractor] Kingdom with name {} not found", to);
            return;
        }

        toKingdom.get().receiveResourceTransfer(resource, amount);
        kingdomRepository.update(from);
        kingdomRepository.update(toKingdom.get());
        notificationSystem.create(to, NotificationKind.delivery, Utils.format("{} carriers delivered {} {}", from.getName(), amount, resource));
    }

    @Override
    public void resolveAttack(KingdomEntity attackerKingdom, KingdomOngoingAttackEntity attack)
    {
        log.info("[KingdomInteractor] Resolving attack from {} on {}", attackerKingdom.getName(), attack.targetKingdomName);

        var maybeDefendantKingdom = kingdomRepository.getKingdomByName(attack.targetKingdomName);
        if (maybeDefendantKingdom.isEmpty())
        {
            log.warn("[KingdomInteractor] Kingdom with name {} not found", attack.targetKingdomName);
            returnSurvivors(attackerKingdom, attack.units);
            return;
        }

        var defendantKingdom = maybeDefendantKingdom.get();
        var unitsSent = new UnitsMapDto(attack.units);
        var defenders = new DefendingForces(defendantKingdom);
        var defenderUnitsBefore = defenders.snapshot();
        var alliesBefore = defenders.alliedSnapshot();
        var garrisonsBefore = defenders.garrisons().stream().collect(Collectors.toMap(garrison -> garrison, garrison -> garrison.getUnits().countMilitary()));

        processDefenseSalvo(attackerKingdom, defendantKingdom, attack);
        processAttackSalvo(defendantKingdom, attack);
        var attackerArrowLosses = difference(unitsSent, attack.units);
        var defenderArrowLosses = difference(defenderUnitsBefore, defenders.snapshot());
        processMelee(attackerKingdom, defendantKingdom, attack);

        var attackPower = detailsProvider.getAttackPower(attack.units);
        var defensePower = detailsProvider.getDefensePower(defendantKingdom);
        var attackerWon = attackPower > defensePower;
        processGuardLosses(defendantKingdom, attackPower, defensePower);
        var landCaptured = 0;
        Map<ResourceName, Integer> plunder = new EnumMap<>(ResourceName.class);
        if (attackerWon)
        {
            defendantKingdom.setProtectionTicks(gameConfig.attack().protectionTicksAfterDefeat());
            var dominance = getDominance(attackPower, defensePower);
            if (attack.attackType == AttackType.land)
            {
                landCaptured = captureLand(attackerKingdom, defendantKingdom, dominance);
            } else
            {
                plunder = plunder(attackerKingdom, defendantKingdom, dominance);
            }
        }

        returnSurvivors(attackerKingdom, attack.units);

        var report = new BattleReportDto(Id.generate(), Instant.now(), attackerKingdom.getName(), defendantKingdom.getName(), attack.attackType,
                unitsSent, difference(unitsSent, attack.units), difference(defenderUnitsBefore, defenders.snapshot()), attackerArrowLosses, defenderArrowLosses,
                difference(alliesBefore, defenders.alliedSnapshot()), attackPower, defensePower, attackerWon, landCaptured, plunder);
        warReportRepository.addBattleReport(report);
        notifyAboutBattle(report);
        notifyGarrisons(report, garrisonsBefore);

        kingdomRepository.update(attackerKingdom);
        kingdomRepository.update(defendantKingdom);

        landJoinedArmies(attack);
    }

    @Override
    public boolean waitsAtBorder(KingdomOngoingAttackEntity attack)
    {
        var lead = attack.getJoinsAttackId();
        return lead != null && kingdomRepository.getAttacksOn(attack.getTargetKingdomName()).stream().anyMatch(other -> other.getId().equals(lead));
    }

    /**
     * allies' armies waiting at the border fight right after the army whose strike they joined, each in its own battle
     */
    private void landJoinedArmies(KingdomOngoingAttackEntity lead)
    {
        var waiting = kingdomRepository.getAttacksOn(lead.getTargetKingdomName()).stream().filter(attack -> attack.joins(lead) && attack.turnsLeft <= 0).toList();
        for (var attack : waiting)
        {
            var ally = attack.getKingdom();
            ally.getOngoingAttacks().remove(attack);
            resolveAttack(ally, attack);
        }
    }

    private void returnSurvivors(KingdomEntity attackerKingdom, UnitsMapDto survivors)
    {
        for (UnitName unitName : UnitName.getMilitaryUnits())
        {
            attackerKingdom.getUnits().moveMobileToAvailable(unitName, survivors.getCount(unitName));
        }
    }

    private void notifyAboutBattle(BattleReportDto report)
    {
        var spoils = report.attackType() == AttackType.land ? Utils.format("{} land", report.landCaptured()) : Utils.format("{} gold and supplies", report.plunder().getOrDefault(ResourceName.gold, 0));
        if (report.attackerWon())
        {
            notificationSystem.create(report.attacker(), NotificationKind.victory, Utils.format("Victory! Our army defeated {} and took {}", report.defender(), spoils));
            notificationSystem.create(report.defender(), NotificationKind.breached, Utils.format("{} attacked us and won, we lost {}", report.attacker(), spoils));
        } else
        {
            notificationSystem.create(report.attacker(), NotificationKind.defeat, Utils.format("Defeat! Our army was repelled by {}", report.defender()));
            notificationSystem.create(report.defender(), NotificationKind.repelled, Utils.format("We repelled an attack from {}", report.attacker()));
        }
    }

    /**
     * every ally whose soldiers stood in the battle hears how it went and what it cost them; an emptied garrison is gone
     */
    private void notifyGarrisons(BattleReportDto report, Map<KingdomReinforcementEntity, Integer> garrisonsBefore)
    {
        garrisonsBefore.forEach((garrison, before) ->
        {
            var owner = garrison.getKingdom();
            var fallen = before - garrison.getUnits().countMilitary();
            if (report.attackerWon())
            {
                notificationSystem.create(owner.getName(), NotificationKind.breached,
                        Utils.format("{} broke through at {}, where {} of our soldiers fell", report.attacker(), report.defender(), fallen));
            } else
            {
                notificationSystem.create(owner.getName(), NotificationKind.repelled,
                        Utils.format("Our soldiers at {} helped repel {}, {} of them fell", report.defender(), report.attacker(), fallen));
            }
            if (garrison.getUnits().countMilitary() == 0)
            {
                garrison.goHome();
            }
            kingdomRepository.update(owner);
        });
    }

    /**
     * a garrison sent by an ally has arrived and stands guard
     */
    @Override
    public void reinforcementsArrived(KingdomReinforcementEntity reinforcement)
    {
        notificationSystem.create(reinforcement.getHost().getName(), NotificationKind.alliance,
                Utils.format("{} soldiers from {} now stand guard with us", reinforcement.getUnits().countMilitary(), reinforcement.getKingdom().getName()));
    }

    private static UnitsMapDto difference(UnitsMapDto before, UnitsMapDto after)
    {
        var losses = new UnitsMapDto();
        for (var unit : UnitName.getMilitaryUnits())
        {
            losses.setCount(unit, before.getCount(unit) - after.getCount(unit));
        }
        losses.setCount(UnitName.guard, before.getCount(UnitName.guard) - after.getCount(UnitName.guard));
        return losses;
    }

    /**
     * guards hold the walls to the end, the stronger the attack compared to the defense the more of them fall
     */
    void processGuardLosses(KingdomEntity defendantKingdom, double attackPower, double defensePower)
    {
        var guards = defendantKingdom.getUnits().getAvailableCount(UnitName.guard);
        if (guards <= 0 || attackPower <= 0)
        {
            return;
        }

        var share = attackPower / (attackPower + defensePower);
        var guardsLost = (int) Math.floor(guards * share * gameConfig.attack().maxGuardLossRatio());
        defendantKingdom.getUnits().subtractAvailableCount(UnitName.guard, guardsLost);
    }

    /**
     * How decisive the victory was, from 0 (barely won) to 1 (attack at least twice as strong as defense)
     */
    static double getDominance(double attackPower, double defensePower)
    {
        if (defensePower <= 0)
        {
            return 1.0;
        }

        return Math.clamp(attackPower / defensePower - 1, 0.0, 1.0);
    }

    /**
     * the share of a smaller target's land a won attack can take: the full share against a kingdom at least the attacker's
     * size, less the smaller the target, so a large kingdom gains little from beating small ones
     */
    double sizeCaptureFactor(int attackerLand, int defenderLand)
    {
        var relativeSize = Math.min(1.0, (double) defenderLand / Math.max(1, attackerLand));
        return Math.pow(relativeSize, gameConfig.attack().smallerTargetCaptureExponent());
    }

    int captureLand(KingdomEntity attacker, KingdomEntity defender, double dominance)
    {
        var defenderLand = defender.getResources().getCount(ResourceName.land);
        var sizeFactor = sizeCaptureFactor(attacker.getResources().getCount(ResourceName.land), defenderLand);
        var landCaptured = Math.max(1, (int) Math.floor(defenderLand * gameConfig.attack().maxLandCaptureRatio() * dominance * sizeFactor));
        landCaptured = Math.min(landCaptured, defenderLand);

        var buildingsToRaze = landCaptured - Math.max(0, defender.getUnusedLand());
        if (buildingsToRaze > 0)
        {
            razeBuildings(defender, buildingsToRaze);
        }

        defender.getResources().subtractCount(ResourceName.land, landCaptured);
        attacker.getResources().addCount(ResourceName.land, landCaptured);
        return landCaptured;
    }

    /**
     * buildings on captured land are destroyed proportionally to how many of each the defender has
     */
    static void razeBuildings(KingdomEntity kingdom, int count)
    {
        var buildings = kingdom.getBuildings();
        var total = buildings.countAll();
        var toRaze = Math.min(count, total);
        var razed = 0;
        for (var building : BuildingName.values())
        {
            var share = (int) Math.floor((double) buildings.getCount(building) / total * toRaze);
            buildings.subtractCount(building, share);
            razed += share;
        }

        while (razed < toRaze)
        {
            for (var building : BuildingName.values())
            {
                if (razed < toRaze && buildings.getCount(building) > 0)
                {
                    buildings.subtractCount(building, 1);
                    razed++;
                }
            }
        }
    }

    Map<ResourceName, Integer> plunder(KingdomEntity attacker, KingdomEntity defender, double dominance)
    {
        var attackConfig = gameConfig.attack();
        var warehouseLevels = defender.getSpecialBuildings().stream().filter(building -> building.buildingType == SpecialBuildingType.warehouse).mapToInt(building -> building.level).sum();
        var protection = Math.min(attackConfig.maxWarehousePlunderProtection(), warehouseLevels * attackConfig.warehousePlunderProtectionPerLevel());
        var plunderRatio = attackConfig.maxPlunderRatio() * dominance * (1 - protection);

        Map<ResourceName, Integer> plunder = new EnumMap<>(ResourceName.class);
        for (var resource : plunderableResources)
        {
            var amount = (int) Math.floor(defender.getResources().getCount(resource) * plunderRatio);
            defender.getResources().subtractCount(resource, amount);
            attacker.getResources().addCount(resource, amount);
            plunder.put(resource, amount);
        }
        return plunder;
    }

    void processDefenseSalvo(KingdomEntity attackerKingdom, KingdomEntity defenderKingdom, KingdomOngoingAttackEntity attack)
    {
        var defenderBowmenCount = new DefendingForces(defenderKingdom).count(UnitName.bowman);
        var attackerUnits = attack.units;
        var attackerUnitsRatios = attack.units.getMilitaryUnitsRatios();

        var bowmenHittingBowmen = (int) Math.ceil(attackerUnitsRatios.get(UnitName.bowman) * defenderBowmenCount);
        var bowmenHittingInfantry = (int) Math.ceil(attackerUnitsRatios.get(UnitName.infantry) * defenderBowmenCount);
        var bowmenHittingCavarly = (int) Math.ceil(attackerUnitsRatios.get(UnitName.cavalry) * defenderBowmenCount);

        // TODO move consts to configuration
        var killedAttackingBowmen = Math.min(bowmenHittingBowmen / 3, attackerUnits.getCount(UnitName.bowman));
        var killedAttackingInfantry = Math.min(bowmenHittingInfantry / 5, attackerUnits.getCount(UnitName.infantry));
        var killedAttackingCavalry = Math.min(bowmenHittingCavarly / 7, attackerUnits.getCount(UnitName.cavalry));

        attackerUnits.subtractCount(UnitName.bowman, killedAttackingBowmen);
        attackerUnits.subtractCount(UnitName.infantry, killedAttackingInfantry);
        attackerUnits.subtractCount(UnitName.cavalry, killedAttackingCavalry);

        attackerKingdom.getUnits().subtractMobileCount(UnitName.bowman, killedAttackingBowmen);
        attackerKingdom.getUnits().subtractMobileCount(UnitName.infantry, killedAttackingInfantry);
        attackerKingdom.getUnits().subtractMobileCount(UnitName.cavalry, killedAttackingCavalry);

        log.info("[KingdomInteractor] Defender bowmen salvo killed {} bowmen, {} infantry, {} cavalry from attacking kingdom {}", killedAttackingBowmen, killedAttackingInfantry, killedAttackingCavalry, attackerKingdom.getName());
    }

    void processAttackSalvo(KingdomEntity defendantKingdom, KingdomOngoingAttackEntity attack)
    {
        var attackerBowmenCount = attack.units.getCount(UnitName.bowman);
        var defenders = new DefendingForces(defendantKingdom);
        var defenderUnitsRatios = defenders.militaryRatios();

        var bowmenHittingBowmen = (int) Math.ceil(defenderUnitsRatios.get(UnitName.bowman) * attackerBowmenCount);
        var bowmenHittingInfantry = (int) Math.ceil(defenderUnitsRatios.get(UnitName.infantry) * attackerBowmenCount);
        var bowmenHittingCavarly = (int) Math.ceil(defenderUnitsRatios.get(UnitName.cavalry) * attackerBowmenCount);

        var killedDefendingBowmen = Math.min(bowmenHittingBowmen / 3, defenders.count(UnitName.bowman));
        var killedDefendingInfantry = Math.min(bowmenHittingInfantry / 5, defenders.count(UnitName.infantry));
        var killedDefendingCavalry = Math.min(bowmenHittingCavarly / 7, defenders.count(UnitName.cavalry));

        defenders.lose(UnitName.bowman, killedDefendingBowmen);
        defenders.lose(UnitName.infantry, killedDefendingInfantry);
        defenders.lose(UnitName.cavalry, killedDefendingCavalry);

        log.info("[KingdomInteractor] Attacker bowmen salvo killed {} bowmen, {} infantry, {} cavalry from defending kingdom {}", killedDefendingBowmen, killedDefendingInfantry, killedDefendingCavalry, defendantKingdom.getName());
    }

    /**
     * infantry and cavalry strike, their blows spread over the enemy army by its make-up; bowmen strike nobody in melee
     * but are among those struck, and every type takes its own number of blows to fall
     */
    void processMelee(KingdomEntity attackerKingdom, KingdomEntity defendantKingdom, KingdomOngoingAttackEntity attack)
    {
        var attackingUnits = attack.units;
        var defenders = new DefendingForces(defendantKingdom);
        var attackerStrikers = attackingUnits.getCount(UnitName.infantry) + attackingUnits.getCount(UnitName.cavalry);
        var defenderStrikers = defenders.count(UnitName.infantry) + defenders.count(UnitName.cavalry);
        var attackingUnitsRatios = attackingUnits.getMilitaryUnitsRatios();
        var defenderUnitsRatios = defenders.militaryRatios();

        for (var unit : UnitName.getMilitaryUnits())
        {
            var blowsToFall = meleeBlowsToFall.get(unit);
            var killedAttacking = Math.min((int) Math.ceil(defenderStrikers * attackingUnitsRatios.get(unit)) / blowsToFall, attackingUnits.getCount(unit));
            var killedDefending = Math.min((int) Math.ceil(attackerStrikers * defenderUnitsRatios.get(unit)) / blowsToFall, defenders.count(unit));

            attackingUnits.subtractCount(unit, killedAttacking);
            attackerKingdom.getUnits().subtractMobileCount(unit, killedAttacking);
            defenders.lose(unit, killedDefending);
        }
    }
}
