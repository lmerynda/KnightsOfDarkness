package com.knightsofdarkness.web.bots;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knightsofdarkness.common.kingdom.BuildingName;
import com.knightsofdarkness.common.kingdom.ResourceName;
import com.knightsofdarkness.common.kingdom.UnitName;
import com.knightsofdarkness.common.kingdom.UnitsMapDto;
import com.knightsofdarkness.common.market.MarketResource;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.kingdom.model.KingdomBuildAction;
import com.knightsofdarkness.web.kingdom.model.KingdomDetailsProvider;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;
import com.knightsofdarkness.web.kingdom.model.KingdomOtherAction;
import com.knightsofdarkness.web.kingdom.model.KingdomSpecialBuildingAction;
import com.knightsofdarkness.web.kingdom.model.KingdomTrainAction;
import com.knightsofdarkness.web.market.IMarket;

public final class BotFunctions {
    private static final Logger log = LoggerFactory.getLogger(BotFunctions.class);
    private final GameConfig gameConfig;
    private final KingdomEntity kingdom;
    private final KingdomDetailsProvider kingdomDetailsProvider;

    public BotFunctions(IBot bot, GameConfig gameConfig)
    {
        this.gameConfig = gameConfig;
        this.kingdom = bot.getKingdom();
        this.kingdomDetailsProvider = bot.getKingdomDetailsProvider();
    }

    public int buyFoodForUpkeep(IMarket market)
    {
        var foodAmount = kingdom.getResources().getCount(ResourceName.food);
        var foodUpkeep = kingdomDetailsProvider.getFoodUpkeep(kingdom);
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

    public int buyEnoughIronToMaintainFullProduction(KingdomEntity kingdom, IMarket market)
    {
        var blacksmithProduction = kingdom.getUnits().getAvailableCount(UnitName.blacksmith) * gameConfig.production().getProductionRate(UnitName.blacksmith);
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

    public int buyLandToMaintainUnused(KingdomEntity kingdom, int count)
    {
        var unusedLand = kingdom.getUnusedLand();
        assert unusedLand >= 0;
        if (unusedLand < 2)
        {
            var action = new KingdomOtherAction(kingdom);
            return action.buyLand(2).amount();
        }

        return 0;
    }

    public int buildSpecialistBuilding(KingdomEntity kingdom, BuildingName building, int count)
    {
        var unit = UnitName.getByBuilding(building);
        var unitCount = kingdom.getUnits().getAvailableCount(unit);
        var fullCapacity = kingdomDetailsProvider.getBuildingCapacity(kingdom, building);
        var freeCapacity = fullCapacity - unitCount;
        var perBuildingCapacity = gameConfig.buildingCapacity().getCapacity(building);
        var desiredFreeCapacity = perBuildingCapacity * count;
        if (freeCapacity >= desiredFreeCapacity)
        {
            return 0;
        }

        var lackingCapacity = desiredFreeCapacity - freeCapacity;
        var buildingsToBuild = (int) Math.ceil((double) lackingCapacity / perBuildingCapacity);
        var kingdomBuildings = new KingdomBuildingsEntityDto();
        kingdomBuildings.setCount(building, buildingsToBuild);

        var action = new KingdomBuildAction(kingdom, gameConfig);
        return action.build(kingdomBuildings).buildings().countAll();
    }

    public int trainUnits(KingdomEntity kingdom, UnitName unit, int count)
    {
        var toTrain = new UnitsMapDto();
        toTrain.setCount(unit, count);
        var action = new KingdomTrainAction(kingdom, gameConfig);
        var trainedUnits = action.train(toTrain);
        return trainedUnits.units().countAll();
    }

    public int buyToolsToMaintainCount(IMarket market, KingdomEntity kingdom, int count)
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

    public int trainBuilders(KingdomEntity kingdom, int count, double desiredBuilderToSpecialistRatio)
    {
        int builderCount = kingdom.getUnits().getAvailableCount(UnitName.builder);
        int unitCount = kingdom.getTotalPeopleCount();
        double currentBuilderRatio = (double) builderCount / unitCount;

        if (currentBuilderRatio >= desiredBuilderToSpecialistRatio)
        {
            // we have enough builders already
            return 0;
        }

        var toTrain = new UnitsMapDto();
        var action = new KingdomTrainAction(kingdom, gameConfig);
        var trainedUnits = action.train(toTrain);
        return trainedUnits.units().countAll();
    }

    public int buildHouses(KingdomEntity kingdom, int count, double desiredHousesToSpecialistBuildingRatio)
    {
        int housesCount = kingdom.getBuildings().getCount(BuildingName.house);
        int buildingsCount = kingdom.getBuildings().countAll();
        double currentHousesRatio = (double) housesCount / buildingsCount;

        if (currentHousesRatio >= desiredHousesToSpecialistBuildingRatio)
        {
            // we have enough houses already
            return 0;
        }

        var kingdomBuildings = new KingdomBuildingsEntityDto();
        kingdomBuildings.setCount(BuildingName.house, count);

        var action = new KingdomBuildAction(kingdom, gameConfig);
        return action.build(kingdomBuildings).buildings().countAll();
    }

    public boolean doesHaveEnoughFoodForNextTurn(KingdomEntity kingdom)
    {
        var foodAvailable = kingdom.getResources().getCount(ResourceName.food);
        var foodUpkeep = kingdomDetailsProvider.getFoodUpkeep(kingdom);
        var foodReserve = (double) foodAvailable / foodUpkeep;
        log.info("[{}] food reserve {}", kingdom.getName(), foodReserve);

        return foodReserve >= 0.8d;
    }

    public void withdrawAllOffers(KingdomEntity kingdom, IMarket market)
    {
        // TODO make it more sophisticated to withdraw only necessary amount
        var offers = market.getOffersByKingdomName(kingdom.getName());
        for (var offer : offers)
        {
            market.removeOffer(offer);
        }
    }

    public int putRemainingPointsToLowestLevelSpecialBuilding(KingdomEntity kingdom)
    {
        var action = new KingdomSpecialBuildingAction(kingdom, gameConfig);
        var maybeSpecialBuilding = action.getLowestLevelSpecialBuilding();
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
        return action.buildSpecialBuilding(specialBuilding, buildingPoints);
    }
}
