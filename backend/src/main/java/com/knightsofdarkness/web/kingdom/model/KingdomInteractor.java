package com.knightsofdarkness.web.kingdom.model;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knightsofdarkness.web.common.kingdom.AttackType;
import com.knightsofdarkness.web.common.kingdom.BuildingName;
import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.kingdom.SpecialBuildingType;
import com.knightsofdarkness.web.common.kingdom.UnitName;
import com.knightsofdarkness.web.common.kingdom.UnitsMapDto;
import com.knightsofdarkness.web.common.market.MarketResource;
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
        notificationSystem.create(to, Utils.format("{} carriers delivered {} {}", from.getName(), amount, resource));
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
        var defenderUnitsBefore = new UnitsMapDto(defendantKingdom.getUnits().getAvailableUnits());

        processDefenseSalvo(attackerKingdom, defendantKingdom, attack);
        processAttackSalvo(defendantKingdom, attack);
        var attackerArrowLosses = difference(unitsSent, attack.units);
        var defenderArrowLosses = difference(defenderUnitsBefore, new UnitsMapDto(defendantKingdom.getUnits().getAvailableUnits()));
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
                unitsSent, difference(unitsSent, attack.units), difference(defenderUnitsBefore, new UnitsMapDto(defendantKingdom.getUnits().getAvailableUnits())),
                attackerArrowLosses, defenderArrowLosses, attackPower, defensePower, attackerWon, landCaptured, plunder);
        warReportRepository.addBattleReport(report);
        notifyAboutBattle(report);

        kingdomRepository.update(attackerKingdom);
        kingdomRepository.update(defendantKingdom);
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
            notificationSystem.create(report.attacker(), Utils.format("Victory! Our army defeated {} and took {}", report.defender(), spoils));
            notificationSystem.create(report.defender(), Utils.format("{} attacked us and won, we lost {}", report.attacker(), spoils));
        } else
        {
            notificationSystem.create(report.attacker(), Utils.format("Defeat! Our army was repelled by {}", report.defender()));
            notificationSystem.create(report.defender(), Utils.format("We repelled an attack from {}", report.attacker()));
        }
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

    int captureLand(KingdomEntity attacker, KingdomEntity defender, double dominance)
    {
        var defenderLand = defender.getResources().getCount(ResourceName.land);
        var landCaptured = Math.max(1, (int) Math.floor(defenderLand * gameConfig.attack().maxLandCaptureRatio() * dominance));
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
        var defenderBowmenCount = defenderKingdom.getUnits().getAvailableCount(UnitName.bowman);
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
        var defenderUnits = defendantKingdom.units;
        var defenderUnitsRatios = defenderUnits.getAvailableMilitaryUnitsRatios();

        var bowmenHittingBowmen = (int) Math.ceil(defenderUnitsRatios.get(UnitName.bowman) * attackerBowmenCount);
        var bowmenHittingInfantry = (int) Math.ceil(defenderUnitsRatios.get(UnitName.infantry) * attackerBowmenCount);
        var bowmenHittingCavarly = (int) Math.ceil(defenderUnitsRatios.get(UnitName.cavalry) * attackerBowmenCount);

        var killedDefendingBowmen = Math.min(bowmenHittingBowmen / 3, defenderUnits.getAvailableCount(UnitName.bowman));
        var killedDefendingInfantry = Math.min(bowmenHittingInfantry / 5, defenderUnits.getAvailableCount(UnitName.infantry));
        var killedDefendingCavalry = Math.min(bowmenHittingCavarly / 7, defenderUnits.getAvailableCount(UnitName.cavalry));

        defenderUnits.subtractAvailableCount(UnitName.bowman, killedDefendingBowmen);
        defenderUnits.subtractAvailableCount(UnitName.infantry, killedDefendingInfantry);
        defenderUnits.subtractAvailableCount(UnitName.cavalry, killedDefendingCavalry);

        log.info("[KingdomInteractor] Attacker bowmen salvo killed {} bowmen, {} infantry, {} cavalry from defending kingdom {}", killedDefendingBowmen, killedDefendingInfantry, killedDefendingCavalry, defendantKingdom.getName());
    }

    /**
     * infantry and cavalry strike, their blows spread over the enemy army by its make-up; bowmen strike nobody in melee
     * but are among those struck, and every type takes its own number of blows to fall
     */
    void processMelee(KingdomEntity attackerKingdom, KingdomEntity defendantKingdom, KingdomOngoingAttackEntity attack)
    {
        var attackingUnits = attack.units;
        var defenderUnits = defendantKingdom.units;
        var attackerStrikers = attackingUnits.getCount(UnitName.infantry) + attackingUnits.getCount(UnitName.cavalry);
        var defenderStrikers = defenderUnits.getAvailableCount(UnitName.infantry) + defenderUnits.getAvailableCount(UnitName.cavalry);
        var attackingUnitsRatios = attackingUnits.getMilitaryUnitsRatios();
        var defenderUnitsRatios = defenderUnits.getAvailableMilitaryUnitsRatios();

        for (var unit : UnitName.getMilitaryUnits())
        {
            var blowsToFall = meleeBlowsToFall.get(unit);
            var killedAttacking = Math.min((int) Math.ceil(defenderStrikers * attackingUnitsRatios.get(unit)) / blowsToFall, attackingUnits.getCount(unit));
            var killedDefending = Math.min((int) Math.ceil(attackerStrikers * defenderUnitsRatios.get(unit)) / blowsToFall, defenderUnits.getAvailableCount(unit));

            attackingUnits.subtractCount(unit, killedAttacking);
            attackerKingdom.getUnits().subtractMobileCount(unit, killedAttacking);
            defenderUnits.subtractAvailableCount(unit, killedDefending);
        }
    }
}
