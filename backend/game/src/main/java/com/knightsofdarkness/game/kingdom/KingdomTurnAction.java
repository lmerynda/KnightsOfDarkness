package com.knightsofdarkness.game.kingdom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knightsofdarkness.common.kingdom.BuildingName;
import com.knightsofdarkness.common.kingdom.KingdomPassTurnActionResult;
import com.knightsofdarkness.common.kingdom.KingdomTurnReport;
import com.knightsofdarkness.common.kingdom.ResourceName;
import com.knightsofdarkness.common.kingdom.SpecialBuildingType;
import com.knightsofdarkness.common.kingdom.UnitName;
import com.knightsofdarkness.game.interactions.IKingdomInteractor;

public class KingdomTurnAction {
    private static final Logger log = LoggerFactory.getLogger(KingdomTurnAction.class);
    private final Kingdom kingdom;
    private final KingdomTurnReport results;
    private final IKingdomInteractor kingdomInteractor;

    public KingdomTurnAction(Kingdom kingdom, IKingdomInteractor kingdomInteractor)
    {
        this.kingdom = kingdom;
        this.results = new KingdomTurnReport();
        this.kingdomInteractor = kingdomInteractor;
    }

    public KingdomPassTurnActionResult passTurn()
    {
        if (kingdom.getResources().getCount(ResourceName.turns) <= 0)
        {
            return KingdomPassTurnActionResult.failure("Not enough turns");
        }

        kingdom.getResources().subtractCount(ResourceName.turns, 1);
        resetBuildingPoints();
        proffesionalsLeavingDueToInsufficientBuildings();
        peopleLeavingDueToInsuficientHousing();
        double nourishmentProductionFactor = eatFood();
        results.nourishmentProductionFactor = nourishmentProductionFactor;
        doProduction(nourishmentProductionFactor);
        // TODO food production should happen before consumption
        getNewPeople();
        processUnitsOnTheMove();

        kingdom.lastTurnReport = results;

        return KingdomPassTurnActionResult.success("Turn passed", results);
    }

    private void processUnitsOnTheMove()
    {
        var allCarriersOnTheMove = kingdom.getCarriersOnTheMove();
        allCarriersOnTheMove.forEach(carriersOnTheMove -> carriersOnTheMove.turnsLeft--);
        allCarriersOnTheMove.stream().filter(carriersOnTheMove -> carriersOnTheMove.turnsLeft <= 0).forEach(carriersOnTheMove ->
        {
            log.info("Carriers on the move arrived at destination: {}", carriersOnTheMove);
            kingdom.getUnits().moveMobileToAvailable(UnitName.carrier, carriersOnTheMove.carriersCount);
            kingdomInteractor.transferResources(kingdom, carriersOnTheMove.targetKingdomName, carriersOnTheMove.resource, carriersOnTheMove.resourceCount);

        });
        allCarriersOnTheMove.removeIf(carriersOnTheMove -> carriersOnTheMove.turnsLeft <= 0);
    }

    private void peopleLeavingDueToInsuficientHousing()
    {
        var housingCapacity = kingdom.getBuildings().getCount(BuildingName.house) * kingdom.getConfig().buildingCapacity().getCapacity(BuildingName.house);
        var peopleCount = kingdom.getTotalPeopleCount();
        if (housingCapacity >= peopleCount)
        {
            return;
        }

        int peopleToExile = peopleCount - housingCapacity;
        int unemployed = kingdom.getResources().getCount(ResourceName.unemployed);
        if (unemployed >= peopleToExile)
        {
            kingdom.getResources().subtractCount(ResourceName.unemployed, peopleToExile);
            results.exiledPeople = peopleToExile;
            return;
        }

        fireProfessionalsToMaintainUnemployedCount(peopleToExile);

        kingdom.getResources().subtractCount(ResourceName.unemployed, peopleToExile);
        results.exiledPeople = peopleToExile;
    }

    private void fireProfessionalsToMaintainUnemployedCount(int peopleToExile)
    {
        int professionalsToFire = peopleToExile - kingdom.getResources().getCount(ResourceName.unemployed);
        fireProfessionalsBasedOnRatios(professionalsToFire);
        professionalsToFire = peopleToExile - kingdom.getResources().getCount(ResourceName.unemployed);
        fireProfessionalsBasedOnCount(professionalsToFire);
    }

    private void fireProfessionalsBasedOnRatios(int professionalsToFire)
    {
        var unitsRatios = kingdom.getUnits().getUnitsRatios();
        for (var unit : UnitName.values())
        {
            // we don't want to fire more that we absolutaly have to, we'll take care of the remainder later on
            var toFire = (int) Math.floor(unitsRatios.get(unit) * professionalsToFire);
            kingdom.getUnits().subtractCount(unit, toFire);
            kingdom.getResources().addCount(ResourceName.unemployed, toFire);
            results.addLeavingProfessionals(unit, toFire);
        }
    }

    private void fireProfessionalsBasedOnCount(int professionalsToFire)
    {
        while (professionalsToFire > 0)
        {
            for (var unit : UnitName.values())
            {
                if (professionalsToFire <= 0)
                {
                    return;
                }

                var count = kingdom.getUnits().getCount(unit);
                if (count > 0)
                {
                    kingdom.getUnits().subtractCount(unit, 1);
                    kingdom.getResources().addCount(ResourceName.unemployed, 1);
                    results.addLeavingProfessionals(unit, 1);
                    professionalsToFire--;

                }
            }
        }
    }

    private void proffesionalsLeavingDueToInsufficientBuildings()
    {
        for (var unit : UnitName.values())
        {
            var maybeBuildingType = BuildingName.getByUnit(unit);
            if (maybeBuildingType.isEmpty())
            {
                continue;
            }
            var building = maybeBuildingType.get();
            var capacity = kingdom.getBuildingCapacity(building);
            var count = kingdom.getUnits().getCount(unit);
            if (count > capacity)
            {
                var leaving = count - capacity;
                kingdom.getUnits().subtractCount(unit, leaving);
                kingdom.getResources().addCount(ResourceName.unemployed, leaving);
                results.addLeavingProfessionals(unit, leaving);
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
        // TODO consequences of not having enough food
        int foodAvailable = kingdom.getResources().getCount(ResourceName.food);
        int foodUpkeep = kingdom.getFoodUpkeep();

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

    private void doProduction(double nourishmentProductionFactor)
    {
        var productionConfig = kingdom.getConfig().production();
        var productionBonus = getKingdomSizeProductionBonus(kingdom.getOccupiedLand());

        for (var unitName : UnitName.getProductionUnits())
        {
            var resourceType = productionConfig.getResource(unitName);
            var resourceProduction = kingdom.getUnits().getCount(unitName) * nourishmentProductionFactor * productionConfig.getProductionRate(unitName);
            var specialBuildingBonus = getSpecialBuildingBonus(resourceType);
            if (unitName == UnitName.blacksmith)
            {
                int neededIron = kingdom.getIronUpkeep(nourishmentProductionFactor);
                log.info("needed iron {}", neededIron);
                var maxIronToSpend = Math.min(neededIron, kingdom.getResources().getCount(ResourceName.iron));
                log.info("max iron to spend {}", maxIronToSpend);
                resourceProduction = Math.min(resourceProduction, maxIronToSpend);
                log.info("resourceProduction after iron calculation {}", resourceProduction);
                kingdom.getResources().subtractCount(ResourceName.iron, maxIronToSpend);
                log.info("Actual tools production: {}", (int) Math.round(resourceProduction * productionBonus * specialBuildingBonus));
            }
            int actualProduction = (int) Math.round(resourceProduction * productionBonus * specialBuildingBonus);
            kingdom.getResources().addCount(resourceType, actualProduction);
            results.resourcesProduced.put(resourceType, actualProduction);
            results.specialBuildingBonus.put(resourceType, specialBuildingBonus);
        }
    }

    private double getSpecialBuildingBonus(ResourceName resourceName)
    {
        var specialBuildingType = SpecialBuildingType.fromResource(resourceName);

        var summedLevels = kingdom.getSpecialBuildings().stream()
                .filter(b -> b.getBuildingType() == specialBuildingType)
                .mapToInt(KingdomSpecialBuilding::getLevel).sum();

        // TODO think about how bonuses have worked in the past
        return 1 + summedLevels * kingdom.getConfig().common().specialBuildingPerLevelProductionBonus();
    }

    private void getNewPeople()
    {
        var housingCapacity = kingdom.getBuildings().getCount(BuildingName.house) * kingdom.getConfig().buildingCapacity().getCapacity(BuildingName.house);
        var peopleCount = kingdom.getTotalPeopleCount();
        if (housingCapacity > peopleCount)
        {
            int arrivingPeople = housingCapacity - peopleCount;
            kingdom.getResources().addCount(ResourceName.unemployed, housingCapacity - peopleCount);
            results.arrivingPeople = arrivingPeople;
        }
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
