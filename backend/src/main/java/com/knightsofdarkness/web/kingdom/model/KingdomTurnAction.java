package com.knightsofdarkness.web.kingdom.model;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knightsofdarkness.web.common.kingdom.BuildingName;
import com.knightsofdarkness.web.common.kingdom.KingdomPassTurnActionResult;
import com.knightsofdarkness.web.common.kingdom.KingdomTurnReport;
import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.kingdom.SpecialBuildingType;
import com.knightsofdarkness.web.common.kingdom.UnitName;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.kingdom.IKingdomInteractor;

public class KingdomTurnAction {
    private static final Logger log = LoggerFactory.getLogger(KingdomTurnAction.class);
    private final KingdomEntity kingdom;
    private final KingdomTurnReport results;
    private final IKingdomInteractor kingdomInteractor;
    private final GameConfig config;
    private final KingdomDetailsProvider kingdomDetailsProvider;

    public KingdomTurnAction(KingdomEntity kingdom, IKingdomInteractor kingdomInteractor, GameConfig gameConfig, KingdomDetailsProvider kingdomDetailsProvider)
    {
        this.kingdom = kingdom;
        this.results = new KingdomTurnReport();
        this.kingdomInteractor = kingdomInteractor;
        this.config = gameConfig;
        this.kingdomDetailsProvider = kingdomDetailsProvider;
    }

    public KingdomPassTurnActionResult passTurn(int weaponsProductionPercentage)
    {
        if (kingdom.getResources().getCount(ResourceName.turns) <= 0)
        {
            return KingdomPassTurnActionResult.failure("Not enough turns");
        }

        kingdom.getResources().subtractCount(ResourceName.turns, 1);
        resetBuildingPoints();
        proffesionalsLeavingDueToInsufficientBuildings();
        peopleLeavingDueToInsuficientHousing();
        // farmers eat from their fields, so the harvest comes in before the kingdom eats
        results.weaponsProductionPercentage = weaponsProductionPercentage;
        doProduction(List.of(ResourceName.food), 1.0, weaponsProductionPercentage);
        double nourishmentProductionFactor = eatFood();
        peopleLeavingDueToHunger(nourishmentProductionFactor);
        results.nourishmentProductionFactor = nourishmentProductionFactor;
        var otherResources = ResourceName.productionResourceNames().stream().filter(resource -> resource != ResourceName.food).toList();
        doProduction(otherResources, nourishmentProductionFactor, weaponsProductionPercentage);
        getNewPeople();
        processCarriersOnTheMove();
        processAttacks();

        kingdom.lastTurnReport = results;
        return KingdomPassTurnActionResult.success("Turn passed", results);
    }

    private void processAttacks()
    {
        var allAttacks = kingdom.getOngoingAttacks();
        allAttacks.forEach(attack -> attack.turnsLeft--);
        var finishedAttacks = allAttacks.stream().filter(attack -> attack.turnsLeft <= 0).toList();
        finishedAttacks.forEach(attack ->
        {
            log.info("Attack arrived at the destination: {}", attack);
            kingdomInteractor.resolveAttack(kingdom, attack);
        });
        allAttacks.removeAll(finishedAttacks);
    }

    private void processCarriersOnTheMove()
    {
        var allCarriersOnTheMove = kingdom.getCarriersOnTheMove();
        allCarriersOnTheMove.forEach(carriersOnTheMove -> carriersOnTheMove.turnsLeft--);
        var arrivedCarriers = allCarriersOnTheMove.stream().filter(carriersOnTheMove -> carriersOnTheMove.turnsLeft <= 0).toList();
        arrivedCarriers.forEach(carriersOnTheMove ->
        {
            log.info("Carriers on the move arrived at destination: {}", carriersOnTheMove);
            kingdom.getUnits().moveMobileToAvailable(UnitName.carrier, carriersOnTheMove.carriersCount);
            kingdomInteractor.transferResources(kingdom, carriersOnTheMove.targetKingdomName, carriersOnTheMove.resource, carriersOnTheMove.resourceCount);
        });
        allCarriersOnTheMove.removeAll(arrivedCarriers);
    }

    private void peopleLeavingDueToInsuficientHousing()
    {
        var housingCapacity = getHousingCapacity();
        var peopleCount = kingdom.getHousedPeopleCount();
        if (housingCapacity >= peopleCount)
        {
            return;
        }

        results.exiledPeople = removeCivilians(peopleCount - housingCapacity);
    }

    private void peopleLeavingDueToHunger(double nourishmentProductionFactor)
    {
        if (nourishmentProductionFactor >= 1.0)
        {
            return;
        }

        var hungryPeople = (int) Math.ceil((1 - nourishmentProductionFactor) * kingdom.getHousedPeopleCount() * config.common().starvationLeaveRatio());
        results.starvedPeople = removeCivilians(hungryPeople);
    }

    /**
     * the unemployed leave first, when there are not enough of them, workers are fired to leave as well
     *
     * @return number of people who left the kingdom
     */
    private int removeCivilians(int count)
    {
        var unemployed = kingdom.getResources().getCount(ResourceName.unemployed);
        if (unemployed < count)
        {
            var civilians = Arrays.stream(UnitName.values()).filter(unit -> !unit.isMilitary()).toList();
            dismissProportionally(civilians, count - unemployed);
        }

        var leaving = Math.min(count, kingdom.getResources().getCount(ResourceName.unemployed));
        kingdom.getResources().subtractCount(ResourceName.unemployed, leaving);
        return leaving;
    }

    /**
     * dismisses units that are at home, proportionally to how many of each there are, dismissed units become unemployed
     */
    private void dismissProportionally(List<UnitName> candidates, int count)
    {
        var units = kingdom.getUnits();
        var available = candidates.stream().mapToInt(units::getAvailableCount).sum();
        var toDismiss = Math.min(count, available);
        if (toDismiss <= 0)
        {
            return;
        }

        var dismissed = 0;
        for (var unit : candidates)
        {
            // we don't want to dismiss more than we have to, the remainder is handled one by one below
            var share = (int) Math.floor((double) units.getAvailableCount(unit) / available * toDismiss);
            dismissAvailable(unit, share);
            dismissed += share;
        }

        while (dismissed < toDismiss)
        {
            for (var unit : candidates)
            {
                if (dismissed < toDismiss && units.getAvailableCount(unit) > 0)
                {
                    dismissAvailable(unit, 1);
                    dismissed++;
                }
            }
        }
    }

    private void dismissAvailable(UnitName unit, int count)
    {
        kingdom.getUnits().subtractAvailableCount(unit, count);
        kingdom.getResources().addCount(ResourceName.unemployed, count);
        results.addLeavingProfessionals(unit, count);
    }

    /**
     * every building houses a limited number of its workers, all military units share the barracks
     */
    private void proffesionalsLeavingDueToInsufficientBuildings()
    {
        for (var building : BuildingName.values())
        {
            var housedUnits = Arrays.stream(UnitName.values()).filter(unit -> BuildingName.getByUnit(unit).equals(Optional.of(building))).toList();
            if (housedUnits.isEmpty())
            {
                continue;
            }

            var capacity = kingdom.getBuildingCapacity(building, config);
            var occupancy = housedUnits.stream().mapToInt(kingdom.getUnits()::getTotalCount).sum();
            if (occupancy > capacity)
            {
                dismissProportionally(housedUnits, occupancy - capacity);
            }
        }
    }

    private void resetBuildingPoints()
    {
        kingdom.getResources().setCount(ResourceName.buildingPoints, 0);
    }

    /*
     * returns factor of people who were fed during current cycle
     * for example if we need to feed 10 people and we only have food for 8
     * then the factor of people who were fed is 8/10 = 0.8
     */
    private double eatFood()
    {
        int foodAvailable = kingdom.getResources().getCount(ResourceName.food);
        int foodUpkeep = kingdomDetailsProvider.getFoodUpkeep(kingdom);

        if (foodAvailable >= foodUpkeep)
        {
            kingdom.getResources().subtractCount(ResourceName.food, foodUpkeep);
            results.foodConsumed = foodUpkeep;

            // everyone was fed
            return 1.0;
        }

        // there wasn't food for everyone
        double fedPeopleRatio = (double) foodAvailable / foodUpkeep;
        // TODO real traces
        // System.out.println("There wasn't enough food in " + kingdom.getName() + " only " + fedPeopleRatio + "% were fed");
        kingdom.getResources().subtractCount(ResourceName.food, foodAvailable);
        results.foodConsumed = foodAvailable;
        return fedPeopleRatio;
    }

    private void doProduction(List<ResourceName> resources, double nourishmentProductionFactor, int weaponsProductionPercentage)
    {
        var productionConfig = config.production();
        var sizeProductionBonus = getKingdomSizeProductionBonus(kingdom.getOccupiedLand());

        for (var resourceType : resources)
        {
            var unitName = UnitName.getByProductionResourceType(resourceType);
            var resourceProduction = kingdom.getUnits().getAvailableCount(unitName) * nourishmentProductionFactor * productionConfig.getProductionRate(unitName);
            var specialBuildingBonus = getSpecialBuildingBonus(resourceType);
            if (unitName == UnitName.blacksmith)
            {
                int neededIron = kingdomDetailsProvider.getIronUpkeep(kingdom, nourishmentProductionFactor);
                var maxIronToSpend = Math.min(neededIron, kingdom.getResources().getCount(ResourceName.iron));
                resourceProduction = Math.min(resourceProduction, maxIronToSpend);
                var selectedProductionRate = switch (resourceType)
                {
                    case ResourceName.tools -> (100 - weaponsProductionPercentage) / 100.0;
                    case ResourceName.weapons -> weaponsProductionPercentage / 100.0;
                    default -> {
                        log.error("Unexpected resource type for blacksmith: {}", resourceType);
                        yield 0;
                    }
                };
                resourceProduction = resourceProduction * selectedProductionRate;
                maxIronToSpend = (int) Math.round(maxIronToSpend * selectedProductionRate);
                kingdom.getResources().subtractCount(ResourceName.iron, maxIronToSpend);
            }
            int actualProduction = (int) Math.round(resourceProduction * sizeProductionBonus * specialBuildingBonus);
            kingdom.getResources().addCount(resourceType, actualProduction);
            results.resourcesProduced.put(resourceType, actualProduction);
            results.specialBuildingBonus.put(resourceType, specialBuildingBonus);
        }
    }

    private double getSpecialBuildingBonus(ResourceName resourceName)
    {
        var specialBuildingType = SpecialBuildingType.fromResource(resourceName);

        var summedLevels = kingdom.getSpecialBuildings().stream()
                .filter(b -> b.buildingType == specialBuildingType)
                .mapToInt(specialBuilding -> specialBuilding.level).sum();

        // TODO think about how bonuses have worked in the past
        return 1 + summedLevels * config.common().specialBuildingPerLevelProductionBonus();
    }

    private void getNewPeople()
    {
        var housingCapacity = getHousingCapacity();
        var peopleCount = kingdom.getHousedPeopleCount();
        if (housingCapacity > peopleCount)
        {
            int arrivingPeople = housingCapacity - peopleCount;
            kingdom.getResources().addCount(ResourceName.unemployed, housingCapacity - peopleCount);
            results.arrivingPeople = arrivingPeople;
        }
    }

    private int getHousingCapacity()
    {
        return kingdomDetailsProvider.getHousingCapacity(kingdom);
    }

    double getKingdomSizeProductionBonus(int land)
    {
        if (land >= 1000)
        {
            results.kingdomSizeProductionBonus = 1.0;
            return 1.0;
        }

        var landFactor = Math.max(100, land); // we don't give a bonus for land below 100 to avoid exploits
        var bonus = getBonusFactorBasedOnLand(landFactor);
        results.kingdomSizeProductionBonus = bonus;

        return bonus;
    }

    /**
     * the bonus factor decreases exponentially, max is 5 at 100 land, min is 1 at
     * 1000 land, formula is made up based on manual curve tweaking
     * 
     * @param land
     * @return
     */
    double getBonusFactorBasedOnLand(int land)
    {
        var bonus = 6.5 * Math.exp(-0.0047 * land) - 0.06;
        var minBonus = Math.max(0, bonus);
        return 1 + minBonus;
    }
}
