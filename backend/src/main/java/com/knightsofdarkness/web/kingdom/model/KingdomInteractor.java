package com.knightsofdarkness.web.kingdom.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.knightsofdarkness.web.kingdom.IMonumentFront;
import com.knightsofdarkness.web.messaging.INotificationSystem;
import com.knightsofdarkness.web.monument.model.MonumentEntity;
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
    private final IMonumentFront monuments;

    public KingdomInteractor(IKingdomRepository kingdomRepository, GameConfig gameConfig, INotificationSystem notificationSystem, IWarReportRepository warReportRepository, IMonumentFront monuments)
    {
        this.monuments = monuments;
        this.kingdomRepository = kingdomRepository;
        this.gameConfig = gameConfig;
        this.notificationSystem = notificationSystem;
        this.warReportRepository = warReportRepository;
        this.detailsProvider = new KingdomDetailsProvider(gameConfig);
    }

    @Override
    public void transferResources(KingdomEntity from, String to, MarketResource resource, int count)
    {
        var toKingdom = kingdomRepository.getKingdomByName(to);
        if (toKingdom.isEmpty())
        {
            log.warn("[KingdomInteractor] Kingdom with name {} not found", to);
            return;
        }

        toKingdom.get().receiveResourceTransfer(resource, count);
        kingdomRepository.update(from);
        kingdomRepository.update(toKingdom.get());
        notificationSystem.create(to, NotificationKind.delivery, Utils.format("{} carriers delivered {} {}", from.getName(), count, resource));
    }

    /**
     * the army has landed: the allies' armies waiting at the border for it fight in the same battle, as one army
     */
    @Override
    public void resolveAttack(KingdomEntity attackerKingdom, KingdomOngoingAttackEntity attack)
    {
        var joined = kingdomRepository.getAttacksOn(attack.getTargetKingdomName()).stream().filter(other -> other.joins(attack) && other.turnsLeft <= 0).toList();
        joined.forEach(other -> other.getKingdom().getOngoingAttacks().remove(other));
        var attackers = AttackingForces.of(attackerKingdom, attack, joined);
        if (attack.attackType == AttackType.monument)
        {
            assaultMonument(attackers, attack.targetKingdomName);
        } else
        {
            strikeKingdom(attackers, attack.targetKingdomName, attack.attackType);
        }
    }

    /** who fought on the attacking side, for the defender and everyone who hears of the battle: "a", "a and b", "a, b and c" */
    private static String side(AttackingForces attackers)
    {
        var names = attackers.armies().stream().map(AttackingForces.Army::name).toList();
        return names.size() == 1 ? names.get(0) : String.join(", ", names.subList(0, names.size() - 1)) + " and " + names.get(names.size() - 1);
    }

    /** the allies an army fought beside, for its own victory or defeat: "" alone, " with b and c" together */
    private static String beside(AttackingForces attackers, AttackingForces.Army army)
    {
        var others = attackers.armies().stream().filter(other -> other != army).map(AttackingForces.Army::name).toList();
        if (others.isEmpty())
        {
            return "";
        }
        return " with " + (others.size() == 1 ? others.get(0) : String.join(", ", others.subList(0, others.size() - 1)) + " and " + others.get(others.size() - 1));
    }

    private void returnSurvivors(AttackingForces attackers)
    {
        attackers.armies().forEach(army -> returnSurvivors(army.kingdom(), army.attack().units));
    }

    private void updateAll(AttackingForces attackers)
    {
        attackers.armies().forEach(army -> kingdomRepository.update(army.kingdom()));
    }

    /**
     * The usual battle, against the garrison alone: no towers, castles, guards or the seat's own army. Winning knocks down
     * stages, scaled by dominance, and takes nothing else. A monument has no protection, so the next army may strike at once.
     */
    private void assaultMonument(AttackingForces attackers, String seat)
    {
        log.info("[KingdomInteractor] Resolving assault from {} on the monument at {}", side(attackers), seat);

        var ownAlliance = attackers.lead().kingdom().getAlliance().map(alliance -> alliance.getName());
        var maybeMonument = monuments.findAtSeatForUpdate(seat).filter(found -> ownAlliance.filter(found.getAllianceName()::equals).isEmpty());
        if (maybeMonument.isEmpty())
        {
            // it fell while the army marched, or its alliance has since taken the attackers in
            returnSurvivors(attackers);
            attackers.armies().forEach(army -> notificationSystem.create(army.name(), NotificationKind.defeat, Utils.format("Our army found no enemy monument to assault at {} and came home", seat)));
            updateAll(attackers);
            return;
        }

        var monument = maybeMonument.get();
        var defenders = DefendingForces.atMonument(monument.getStandingGarrison());
        var defenderUnitsBefore = defenders.snapshot();
        var alliesBefore = defenders.alliedSnapshot();
        var garrisonsBefore = defenders.garrisons().stream().collect(Collectors.toMap(garrison -> garrison, garrison -> garrison.getUnits().countMilitary()));

        takeArrows(attackers, defenders.count(UnitName.bowman));
        looseArrows(attackers, defenders);
        var attackerArrowLosses = attackers.armies().stream().map(army -> difference(army.sent(), army.attack().units)).toList();
        var defenderArrowLosses = difference(defenderUnitsBefore, defenders.snapshot());
        melee(attackers, defenders, Map.of());

        var attackPower = detailsProvider.getAttackPower(attackers.units());
        var defensePower = detailsProvider.getDefensePower(defenders);
        var attackerWon = attackPower > defensePower;
        var stoodComplete = monument.isComplete(gameConfig.monument());
        var stagesRazed = attackerWon ? monument.raze(gameConfig.monument().maxRazeRatio() * getDominance(attackPower, defensePower)) : 0;
        var weights = attackers.weights(detailsProvider);
        var totalWeight = Arrays.stream(weights).sum();

        returnSurvivors(attackers);

        var defenderLosses = difference(defenderUnitsBefore, defenders.snapshot());
        var alliedLosses = difference(alliesBefore, defenders.alliedSnapshot());
        for (int index = 0; index < attackers.armies().size(); index++)
        {
            var army = attackers.armies().get(index);
            var report = new BattleReportDto(Id.generate(), Instant.now(), army.name(), seat, AttackType.monument, army.sent(), difference(army.sent(), army.attack().units), defenderLosses,
                    attackerArrowLosses.get(index), defenderArrowLosses, alliedLosses, attackPower, defensePower, attackerWon, 0, new EnumMap<>(ResourceName.class),
                    stagesRazed * weights[index] / totalWeight);
            warReportRepository.addBattleReport(report);
            notificationSystem.create(army.name(), attackerWon ? NotificationKind.victory : NotificationKind.defeat, attackerWon
                    ? Utils.format("Victory! Our army{} stormed the monument of {} and knocked down {} stages", beside(attackers, army), monument.getAllianceName(), MonumentEntity.stages(stagesRazed))
                    : Utils.format("Defeat! Our army{} was repelled at the monument of {}", beside(attackers, army), monument.getAllianceName()));
        }
        if (attackerWon)
        {
            monuments.assaultWon(monument, side(attackers), stoodComplete && !monument.isComplete(gameConfig.monument()));
        } else
        {
            monuments.assaultRepelled(monument, side(attackers));
        }
        notifyMonumentGarrisons(side(attackers), seat, garrisonsBefore);

        updateAll(attackers);
    }

    /**
     * every member whose soldiers fell hears how many; an emptied garrison is gone
     */
    private void notifyMonumentGarrisons(String attackers, String seat, Map<KingdomReinforcementEntity, Integer> garrisonsBefore)
    {
        garrisonsBefore.forEach((garrison, before) ->
        {
            var owner = garrison.getKingdom();
            var fallen = before - garrison.getUnits().countMilitary();
            if (fallen > 0)
            {
                notificationSystem.create(owner.getName(), NotificationKind.alliance, Utils.format("{} of our soldiers fell at the monument at {}, against {}", fallen, seat, attackers));
            }
            if (garrison.getUnits().countMilitary() == 0)
            {
                garrison.goHome();
            }
            kingdomRepository.update(owner);
        });
    }

    private void strikeKingdom(AttackingForces attackers, String target, AttackType attackType)
    {
        log.info("[KingdomInteractor] Resolving attack from {} on {}", side(attackers), target);

        var maybeDefendantKingdom = kingdomRepository.getKingdomByName(target);
        if (maybeDefendantKingdom.isEmpty())
        {
            log.warn("[KingdomInteractor] Kingdom with name {} not found", target);
            returnSurvivors(attackers);
            updateAll(attackers);
            return;
        }

        var defendantKingdom = maybeDefendantKingdom.get();
        var defenders = new DefendingForces(defendantKingdom);
        var defenderUnitsBefore = defenders.snapshot();
        var alliesBefore = defenders.alliedSnapshot();
        var garrisonsBefore = defenders.garrisons().stream().collect(Collectors.toMap(garrison -> garrison, garrison -> garrison.getUnits().countMilitary()));

        takeArrows(attackers, detailsProvider.getSalvoArrows(defendantKingdom));
        looseArrows(attackers, defenders);
        var attackerArrowLosses = attackers.armies().stream().map(army -> difference(army.sent(), army.attack().units)).toList();
        var defenderArrowLosses = difference(defenderUnitsBefore, defenders.snapshot());
        melee(attackers, defenders, defenders.shelteredInCastles(detailsProvider.getBuildingCapacity(defendantKingdom, BuildingName.castle)));

        var attackPower = detailsProvider.getAttackPower(attackers.units());
        var defensePower = detailsProvider.getDefensePower(defendantKingdom);
        var attackerWon = attackPower > defensePower;
        processGuardLosses(defendantKingdom, attackPower, defensePower);
        var landCaptured = new int[attackers.armies().size()];
        List<Map<ResourceName, Integer>> plunder = attackers.armies().stream().map(army -> (Map<ResourceName, Integer>) new EnumMap<ResourceName, Integer>(ResourceName.class)).toList();
        if (attackerWon)
        {
            defendantKingdom.setProtectionTicks(gameConfig.attack().protectionTicksAfterDefeat());
            var dominance = getDominance(attackPower, defensePower);
            if (attackType == AttackType.land)
            {
                landCaptured = captureLand(attackers, defendantKingdom, dominance);
            } else
            {
                plunder = plunder(attackers, defendantKingdom, dominance);
            }
        }

        returnSurvivors(attackers);

        var defenderLosses = difference(defenderUnitsBefore, defenders.snapshot());
        var alliedLosses = difference(alliesBefore, defenders.alliedSnapshot());
        var reports = new ArrayList<BattleReportDto>();
        for (int index = 0; index < attackers.armies().size(); index++)
        {
            var army = attackers.armies().get(index);
            var report = new BattleReportDto(Id.generate(), Instant.now(), army.name(), defendantKingdom.getName(), attackType, army.sent(), difference(army.sent(), army.attack().units),
                    defenderLosses, attackerArrowLosses.get(index), defenderArrowLosses, alliedLosses, attackPower, defensePower, attackerWon, landCaptured[index], plunder.get(index), 0);
            warReportRepository.addBattleReport(report);
            reports.add(report);
            notificationSystem.create(army.name(), attackerWon ? NotificationKind.victory : NotificationKind.defeat,
                    attackerWon ? Utils.format("Victory! Our army{} defeated {} and took {}", beside(attackers, army), report.defender(), spoils(report))
                            : Utils.format("Defeat! Our army{} was repelled by {}", beside(attackers, army), report.defender()));
        }
        var lost = spoils(attackType, Arrays.stream(landCaptured).sum(), plunder.stream().mapToInt(taken -> taken.getOrDefault(ResourceName.gold, 0)).sum(), 0);
        if (attackerWon)
        {
            notificationSystem.create(defendantKingdom.getName(), NotificationKind.breached, Utils.format("{} attacked us and won, we lost {}", side(attackers), lost));
        } else
        {
            notificationSystem.create(defendantKingdom.getName(), NotificationKind.repelled, Utils.format("We repelled an attack from {}", side(attackers)));
        }
        notifyGarrisons(side(attackers), defendantKingdom.getName(), attackerWon, garrisonsBefore);

        updateAll(attackers);
        kingdomRepository.update(defendantKingdom);
    }

    @Override
    public boolean waitsAtBorder(KingdomOngoingAttackEntity attack)
    {
        if (attack.turnsAtBorder >= gameConfig.attack().turnsToWaitAtBorder())
        {
            return false;
        }
        var marching = kingdomRepository.getAttacksOn(attack.getTargetKingdomName());
        var lead = attack.getJoinsAttackId();
        if (lead != null)
        {
            return marching.stream().anyMatch(other -> other.getId().equals(lead));
        }
        // a lead whose ruler passed its turns first waits for the allies' armies that set off with it
        return marching.stream().anyMatch(other -> other.joins(attack) && other.turnsLeft > 0);
    }

    private void returnSurvivors(KingdomEntity attackerKingdom, UnitsMapDto survivors)
    {
        for (UnitName unitName : UnitName.getMilitaryUnits())
        {
            attackerKingdom.getUnits().moveMobileToAvailable(unitName, survivors.getCount(unitName));
        }
    }

    private static String spoils(BattleReportDto report)
    {
        return spoils(report.attackType(), report.landCaptured(), report.plunder().getOrDefault(ResourceName.gold, 0), report.stagesRazed());
    }

    private static String spoils(AttackType attackType, int land, int gold, double stages)
    {
        return switch (attackType)
        {
            case land -> Utils.format("{} land", land);
            case economy -> Utils.format("{} gold and supplies", gold);
            case monument -> Utils.format("{} stages of its monument", MonumentEntity.stages(stages));
        };
    }

    /**
     * every ally whose soldiers stood in the battle hears how it went and what it cost them; an emptied garrison is gone
     */
    private void notifyGarrisons(String attackers, String defender, boolean attackerWon, Map<KingdomReinforcementEntity, Integer> garrisonsBefore)
    {
        garrisonsBefore.forEach((garrison, before) ->
        {
            var owner = garrison.getKingdom();
            var fallen = before - garrison.getUnits().countMilitary();
            if (attackerWon)
            {
                notificationSystem.create(owner.getName(), NotificationKind.breached,
                        Utils.format("{} broke through at {}, where {} of our soldiers fell", attackers, defender, fallen));
            } else
            {
                notificationSystem.create(owner.getName(), NotificationKind.repelled,
                        Utils.format("Our soldiers at {} helped repel {}, {} of them fell", defender, attackers, fallen));
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
        if (reinforcement.isAtMonument())
        {
            notificationSystem.create(reinforcement.getKingdom().getName(), NotificationKind.alliance,
                    Utils.format("{} of our soldiers now stand guard at the monument at {}", reinforcement.getUnits().countMilitary(), reinforcement.toDto().to()));
            return;
        }
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

    /**
     * takes the defender's land and shares it by the attack power each army brought; the defender's size is weighed against
     * the attackers' land averaged by that power, so allies of one size take what one of them would, and many large kingdoms
     * beating a small one gain as little together as each would alone
     *
     * @return the land each army took
     */
    int[] captureLand(AttackingForces attackers, KingdomEntity defender, double dominance)
    {
        var weights = attackers.weights(detailsProvider);
        var totalWeight = Arrays.stream(weights).sum();
        var attackerLand = 0.0;
        for (int index = 0; index < weights.length; index++)
        {
            attackerLand += attackers.armies().get(index).kingdom().getResources().getCount(ResourceName.land) * weights[index] / totalWeight;
        }
        var defenderLand = defender.getResources().getCount(ResourceName.land);
        var sizeFactor = sizeCaptureFactor((int) Math.round(attackerLand), defenderLand);
        var landCaptured = Math.max(1, (int) Math.floor(defenderLand * gameConfig.attack().maxLandCaptureRatio() * dominance * sizeFactor));
        landCaptured = Math.min(landCaptured, defenderLand);

        var buildingsToRaze = landCaptured - Math.max(0, defender.getUnusedLand());
        if (buildingsToRaze > 0)
        {
            razeBuildings(defender, buildingsToRaze);
        }

        defender.getResources().subtractCount(ResourceName.land, landCaptured);
        var shares = AttackingForces.share(landCaptured, weights, unlimited(weights.length));
        for (int index = 0; index < shares.length; index++)
        {
            attackers.armies().get(index).kingdom().getResources().addCount(ResourceName.land, shares[index]);
        }
        return shares;
    }

    private static int[] unlimited(int count)
    {
        var caps = new int[count];
        Arrays.fill(caps, Integer.MAX_VALUE);
        return caps;
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

    /**
     * takes a share of the defender's stores and shares it by the attack power each army brought
     *
     * @return what each army took
     */
    List<Map<ResourceName, Integer>> plunder(AttackingForces attackers, KingdomEntity defender, double dominance)
    {
        var attackConfig = gameConfig.attack();
        var warehouseLevels = defender.getSpecialBuildings().stream().filter(building -> building.buildingType == SpecialBuildingType.warehouse).mapToInt(building -> building.level).sum();
        var protection = Math.min(attackConfig.maxWarehousePlunderProtection(), warehouseLevels * attackConfig.warehousePlunderProtectionPerLevel());
        var plunderRatio = attackConfig.maxPlunderRatio() * dominance * (1 - protection);
        var weights = attackers.weights(detailsProvider);

        var plunder = attackers.armies().stream().map(army -> (Map<ResourceName, Integer>) new EnumMap<ResourceName, Integer>(ResourceName.class)).toList();
        for (var resource : plunderableResources)
        {
            var count = (int) Math.floor(defender.getResources().getCount(resource) * plunderRatio);
            defender.getResources().subtractCount(resource, count);
            var shares = AttackingForces.share(count, weights, unlimited(weights.length));
            for (int index = 0; index < shares.length; index++)
            {
                attackers.armies().get(index).kingdom().getResources().addCount(resource, shares[index]);
                plunder.get(index).put(resource, shares[index]);
            }
        }
        return plunder;
    }

    void processDefenseSalvo(KingdomEntity attackerKingdom, KingdomEntity defenderKingdom, KingdomOngoingAttackEntity attack)
    {
        takeArrows(AttackingForces.alone(attackerKingdom, attack), detailsProvider.getSalvoArrows(defenderKingdom));
    }

    private void takeArrows(AttackingForces attackers, int arrows)
    {
        var attackerUnitsRatios = attackers.militaryRatios();

        var bowmenHittingBowmen = (int) Math.ceil(attackerUnitsRatios.get(UnitName.bowman) * arrows);
        var bowmenHittingInfantry = (int) Math.ceil(attackerUnitsRatios.get(UnitName.infantry) * arrows);
        var bowmenHittingCavarly = (int) Math.ceil(attackerUnitsRatios.get(UnitName.cavalry) * arrows);

        // TODO move consts to configuration
        var killedAttackingBowmen = Math.min(bowmenHittingBowmen / 3, attackers.count(UnitName.bowman));
        var killedAttackingInfantry = Math.min(bowmenHittingInfantry / 5, attackers.count(UnitName.infantry));
        var killedAttackingCavalry = Math.min(bowmenHittingCavarly / 7, attackers.count(UnitName.cavalry));

        attackers.lose(UnitName.bowman, killedAttackingBowmen);
        attackers.lose(UnitName.infantry, killedAttackingInfantry);
        attackers.lose(UnitName.cavalry, killedAttackingCavalry);

        log.info("[KingdomInteractor] Defender bowmen salvo killed {} bowmen, {} infantry, {} cavalry from the army of {}", killedAttackingBowmen, killedAttackingInfantry, killedAttackingCavalry, side(attackers));
    }

    void processAttackSalvo(KingdomEntity defendantKingdom, KingdomOngoingAttackEntity attack)
    {
        looseArrows(AttackingForces.alone(attack.getKingdom(), attack), new DefendingForces(defendantKingdom));
    }

    private void looseArrows(AttackingForces attackers, DefendingForces defenders)
    {
        var attackerBowmenCount = attackers.count(UnitName.bowman);
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

        log.info("[KingdomInteractor] Attacker bowmen salvo killed {} bowmen, {} infantry, {} cavalry from the defenders, against {}", killedDefendingBowmen, killedDefendingInfantry, killedDefendingCavalry, side(attackers));
    }

    /**
     * infantry and cavalry strike, their blows spread over the enemy army by its make-up; bowmen strike nobody in melee
     * but are among those struck, and every type takes its own number of blows to fall, more behind a castle's walls
     */
    void processMelee(KingdomEntity attackerKingdom, KingdomEntity defendantKingdom, KingdomOngoingAttackEntity attack)
    {
        var defenders = new DefendingForces(defendantKingdom);
        melee(AttackingForces.alone(attackerKingdom, attack), defenders, defenders.shelteredInCastles(detailsProvider.getBuildingCapacity(defendantKingdom, BuildingName.castle)));
    }

    private void melee(AttackingForces attackers, DefendingForces defenders, Map<UnitName, Integer> sheltered)
    {
        var attackerStrikers = attackers.count(UnitName.infantry) + attackers.count(UnitName.cavalry);
        var defenderStrikers = defenders.count(UnitName.infantry) + defenders.count(UnitName.cavalry);
        var attackingUnitsRatios = attackers.militaryRatios();
        var defenderUnitsRatios = defenders.militaryRatios();

        for (var unit : UnitName.getMilitaryUnits())
        {
            var blowsToFall = meleeBlowsToFall.get(unit);
            var blowsOnDefenders = (int) Math.ceil(attackerStrikers * defenderUnitsRatios.get(unit));
            var killedAttacking = Math.min((int) Math.ceil(defenderStrikers * attackingUnitsRatios.get(unit)) / blowsToFall, attackers.count(unit));
            var killedDefending = Math.min(defendersFalling(blowsOnDefenders, blowsToFall, sheltered.getOrDefault(unit, 0), defenders.count(unit)), defenders.count(unit));

            attackers.lose(unit, killedAttacking);
            defenders.lose(unit, killedDefending);
        }
    }

    /**
     * blows land evenly on the soldiers of a type, and those sheltered in a castle take more of them to fall
     */
    private int defendersFalling(int blows, int blowsToFall, int sheltered, int count)
    {
        var shelteredShare = count == 0 ? 0.0 : (double) sheltered / count;
        var fallen = blows * (1 - shelteredShare) / blowsToFall + blows * shelteredShare / (blowsToFall * (1 + gameConfig.attack().castleSoldierBonus()));
        return (int) Math.floor(fallen);
    }
}
