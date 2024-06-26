package com.knightsofdarkness.game.bot;

import com.knightsofdarkness.game.kingdom.BuildingName;
import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.game.kingdom.KingdomUnits;
import com.knightsofdarkness.game.kingdom.ResourceName;
import com.knightsofdarkness.game.kingdom.UnitName;
import com.knightsofdarkness.game.market.IMarket;
import com.knightsofdarkness.game.market.MarketResource;

public class BotFunctions {
    public static int buyFoodForUpkeep(Kingdom kingdom, IMarket market)
    {
        var foodAmount = kingdom.getResources().getCount(ResourceName.food);
        var foodUpkeep = kingdom.getFoodUpkeep();
        var amountToBuy = Math.max(0, foodUpkeep - foodAmount);
        var totalBought = 0;

        // TODO accumulation of amountToBuy and totalBought is the same thing
        while (amountToBuy > 0)
        {
            var optionalOffer = market.getCheapestOfferByResource(MarketResource.food);
            if (optionalOffer.isEmpty())
            {
                return totalBought;
            }

            var offer = optionalOffer.get();
            var amountBought = market.buyExistingOffer(offer, kingdom, kingdom, amountToBuy);
            if (amountBought == 0)
            {
                // Could not afford, TODO tests
                return totalBought;
            }
            amountToBuy -= amountBought;
            totalBought += amountBought;
        }

        return totalBought;
    }

    public static int buyEnoughIronToMaintainFullProduction(Kingdom kingdom, IMarket market)
    {
        var blacksmithProduction = kingdom.getUnits().getCount(UnitName.blacksmith) * kingdom.getConfig().production().getProductionRate(UnitName.blacksmith);
        var ironNeeded = blacksmithProduction;

        var ironAmount = kingdom.getResources().getCount(ResourceName.iron);
        var amountToBuy = Math.max(0, ironNeeded - ironAmount);
        var totalBought = 0;

        // TODO accumulation of amountToBuy and totalBought is the same thing
        while (amountToBuy > 0)
        {
            var optionalOffer = market.getCheapestOfferByResource(MarketResource.iron);
            if (optionalOffer.isEmpty())
            {
                return totalBought;
            }

            var offer = optionalOffer.get();
            var amountBought = market.buyExistingOffer(offer, kingdom, kingdom, amountToBuy);
            if (amountBought == 0)
            {
                // Could not afford, TODO tests
                return totalBought;
            }
            amountToBuy -= amountBought;
            totalBought += amountBought;
        }

        return totalBought;
    }

    public static int buyLandToMaintainUnused(Kingdom kingdom, int count)
    {
        var unusedLand = kingdom.getUnusedLand();
        assert unusedLand >= 0;
        if (unusedLand < 2)
        {
            return kingdom.buyLand(2);
        }

        return 0;
    }

    public static int buildSpecialistBuilding(Kingdom kingdom, BuildingName building, int count)
    {
        var unit = UnitName.getByBuilding(building);
        var unitCount = kingdom.getUnits().getCount(unit);
        var fullCapacity = kingdom.getBuildingCapacity(building);
        var freeCapacity = fullCapacity - unitCount;
        var perBuildingCapacity = kingdom.getConfig().buildingCapacity().getCapacity(building);
        var desiredFreeCapacity = perBuildingCapacity * count;
        if (freeCapacity >= desiredFreeCapacity)
        {
            return 0;
        }

        var lackingCapacity = desiredFreeCapacity - freeCapacity;
        var buildingsToBuild = (int) Math.ceil((double) lackingCapacity / perBuildingCapacity);

        return kingdom.build(building, buildingsToBuild);
    }

    public static int trainUnits(Kingdom kingdom, UnitName unit, int count)
    {
        var toTrain = new KingdomUnits();
        toTrain.addCount(unit, count);
        var trainedUnits = kingdom.train(toTrain);
        return trainedUnits.countAll();
    }

    public static int buyToolsToMaintainCount(IMarket market, Kingdom kingdom, int count)
    {
        var optionalOffer = market.getCheapestOfferByResource(MarketResource.tools);
        if (optionalOffer.isEmpty())
        {
            return 0;
        }

        var offer = optionalOffer.get();
        return market.buyExistingOffer(offer, kingdom, kingdom, count);
    }

    public static int trainBuilders(Kingdom kingdom, int count, double desiredBuilderToSpecialistRatio)
    {
        int builderCount = kingdom.getUnits().getCount(UnitName.builder);
        int unitCount = kingdom.getTotalPeopleCount();
        double currentBuilderRatio = (double) builderCount / unitCount;

        if (currentBuilderRatio >= desiredBuilderToSpecialistRatio)
        {
            // we have enough builders already
            return 0;
        }

        var toTrain = new KingdomUnits();
        toTrain.addCount(UnitName.builder, count);
        var trainedUnits = kingdom.train(toTrain);
        return trainedUnits.countAll();
    }

    public static int buildHouses(Kingdom kingdom, int count, double desiredHousesToSpecialistBuildingRatio)
    {
        int housesCount = kingdom.getBuildings().getCount(BuildingName.house);
        int buildingsCount = kingdom.getBuildings().countAll();
        double currentHousesRatio = (double) housesCount / buildingsCount;

        if (currentHousesRatio >= desiredHousesToSpecialistBuildingRatio)
        {
            // we have enough houses already
            return 0;
        }

        return kingdom.build(BuildingName.house, count);
    }
}
