package com.knightsofdarkness.game.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knightsofdarkness.common.kingdom.BuildingName;
import com.knightsofdarkness.common.kingdom.KingdomBuildingsDto;
import com.knightsofdarkness.common.kingdom.KingdomUnitsDto;
import com.knightsofdarkness.common.kingdom.ResourceName;
import com.knightsofdarkness.common.kingdom.UnitName;
import com.knightsofdarkness.common.market.MarketResource;
import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.game.market.IMarket;

public final class BotFunctions {
    private static final Logger log = LoggerFactory.getLogger(BotFunctions.class);

    private BotFunctions()
    {
    }

    public static int buyFoodForUpkeep(Kingdom kingdom, IMarket market)
    {
        var foodAmount = kingdom.getResources().getCount(ResourceName.food);
        var foodUpkeep = kingdom.getFoodUpkeep();
        var amountToBuy = Math.max(0, foodUpkeep - foodAmount);
        log.info("[{}] Food to buy {} to maintain upkeep {}, while kingdom has {}", kingdom.getName(), amountToBuy, foodUpkeep, foodAmount);
        var totalBought = 0;

        // TODO accumulation of amountToBuy and totalBought is the same thing
        while (amountToBuy > 0)
        {
            var optionalOffer = market.getCheapestOfferByResource(MarketResource.food);
            if (optionalOffer.isEmpty())
            {
                log.info("[{}] there were no offers left to buy food", kingdom.getName());
                return totalBought;
            }

            var offer = optionalOffer.get();
            var result = market.buyExistingOffer(offer, offer.getSeller(), kingdom, amountToBuy);
            if (result.count() == 0)
            {
                // Could not afford, TODO tests
                log.info("[{}] didn't have enough gold to buy food", kingdom.getName());
                return totalBought;
            }
            amountToBuy -= result.count();
            totalBought += result.count();
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
            var result = market.buyExistingOffer(offer, offer.getSeller(), kingdom, amountToBuy);
            if (result.count() == 0)
            {
                // Could not afford, TODO tests
                return totalBought;
            }
            amountToBuy -= result.count();
            totalBought += result.count();
        }

        return totalBought;
    }

    public static int buyLandToMaintainUnused(Kingdom kingdom, int count)
    {
        var unusedLand = kingdom.getUnusedLand();
        assert unusedLand >= 0;
        if (unusedLand < 2)
        {
            return kingdom.buyLand(2).amount();
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
        var kingdomBuildings = new KingdomBuildingsDto();
        kingdomBuildings.setCount(building, buildingsToBuild);

        return kingdom.build(kingdomBuildings).buildings().countAll();
    }

    public static int trainUnits(Kingdom kingdom, UnitName unit, int count)
    {
        var toTrain = new KingdomUnitsDto();
        toTrain.setCount(unit, count);
        var trainedUnits = kingdom.train(toTrain);
        return trainedUnits.units().countAll();
    }

    public static int buyToolsToMaintainCount(IMarket market, Kingdom kingdom, int count)
    {
        var optionalOffer = market.getCheapestOfferByResource(MarketResource.tools);
        if (optionalOffer.isEmpty())
        {
            return 0;
        }

        var offer = optionalOffer.get();
        var result = market.buyExistingOffer(offer, offer.getSeller(), kingdom, count);
        return result.count();
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

        var toTrain = new KingdomUnitsDto();
        toTrain.setCount(UnitName.builder, count);
        var trainedUnits = kingdom.train(toTrain);
        return trainedUnits.units().countAll();
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

        var kingdomBuildings = new KingdomBuildingsDto();
        kingdomBuildings.setCount(BuildingName.house, count);

        return kingdom.build(kingdomBuildings).buildings().countAll();
    }

    public static boolean doesHaveEnoughFoodForNextTurn(Kingdom kingdom)
    {
        var foodAvailable = kingdom.getResources().getCount(ResourceName.food);
        var foodUpkeep = kingdom.getFoodUpkeep();
        var foodReserve = (double) foodAvailable / foodUpkeep;
        log.info("[{}] food reserve {}", kingdom.getName(), foodReserve);

        return foodReserve >= 0.8d;
    }

    public static void withdrawAllOffers(Kingdom kingdom, IMarket market)
    {
        // TODO make it more sophisticated to withdraw only necessary amount
        var offers = market.getOffersByKingdomName(kingdom.getName());
        for (var offer : offers)
        {
            market.removeOffer(offer);
        }
    }

    public static int putRemainingPointsToLowestLevelSpecialBuilding(Kingdom kingdom)
    {
        var maybeSpecialBuilding = kingdom.getLowestLevelSpecialBuilding();
        if (maybeSpecialBuilding.isEmpty())
        {
            log.info("[{}] has no special building", kingdom.getName());
            return 0;
        }

        var specialBuilding = maybeSpecialBuilding.get();
        if (specialBuilding.isMaxLevel())
        {
            return 0;
        }

        int buildingPoints = kingdom.getResources().getCount(ResourceName.buildingPoints);
        return kingdom.buildSpecialBuilding(specialBuilding, buildingPoints);
    }

}
