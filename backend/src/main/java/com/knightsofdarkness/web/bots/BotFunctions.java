package com.knightsofdarkness.web.bots;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knightsofdarkness.web.common.kingdom.BuildingName;
import com.knightsofdarkness.web.common.kingdom.KingdomBuildingsDto;
import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.kingdom.UnitName;
import com.knightsofdarkness.web.common.kingdom.UnitsMapDto;
import com.knightsofdarkness.web.common.market.MarketResource;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.kingdom.model.KingdomBuildAction;
import com.knightsofdarkness.web.kingdom.model.KingdomDetailsProvider;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;
import com.knightsofdarkness.web.kingdom.model.KingdomOtherAction;
import com.knightsofdarkness.web.kingdom.model.KingdomSpecialBuildingAction;
import com.knightsofdarkness.web.kingdom.model.KingdomTrainAction;
import com.knightsofdarkness.web.market.IMarket;

public final class BotFunctions
{
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
        var foodCount = kingdom.getResources().getCount(ResourceName.food);
        var foodUpkeep = kingdomDetailsProvider.getFoodUpkeep(kingdom);
        var countToBuy = Math.max(0, foodUpkeep - foodCount);
        log.info("[{}] Food to buy {} to maintain upkeep {}, while kingdom has {}", kingdom.getName(), countToBuy, foodUpkeep, foodCount);
        var totalBought = 0;

        // TODO accumulation of countToBuy and totalBought is the same thing
        while (countToBuy > 0)
        {
            var optionalOffer = market.getCheapestOfferByResource(MarketResource.food);
            if (optionalOffer.isEmpty())
            {
                log.info("[{}] there were no offers left to buy food", kingdom.getName());
                return totalBought;
            }

            var offer = optionalOffer.get();
            var result = BotTransactionalFunctions.buyOffer(market, countToBuy, offer, kingdom);
            if (result.count() == 0)
            {
                // Could not afford, TODO tests
                log.info("[{}] didn't have enough gold to buy food", kingdom.getName());
                return totalBought;
            }
            countToBuy -= result.count();
            totalBought += result.count();
        }

        return totalBought;
    }

    public int buyEnoughIronToMaintainFullProduction(KingdomEntity kingdom, IMarket market)
    {
        var blacksmithProduction = kingdom.getUnits().getAvailableCount(UnitName.blacksmith) * gameConfig.production().getProductionRate(UnitName.blacksmith);
        var ironNeeded = blacksmithProduction;

        var ironCount = kingdom.getResources().getCount(ResourceName.iron);
        var countToBuy = Math.max(0, ironNeeded - ironCount);
        var totalBought = 0;

        // TODO accumulation of countToBuy and totalBought is the same thing
        while (countToBuy > 0)
        {
            var optionalOffer = market.getCheapestOfferByResource(MarketResource.iron);
            if (optionalOffer.isEmpty())
            {
                return totalBought;
            }

            var offer = optionalOffer.get();
            var result = BotTransactionalFunctions.buyOffer(market, countToBuy, offer, kingdom);
            if (result.count() == 0)
            {
                // Could not afford, TODO tests
                return totalBought;
            }
            countToBuy -= result.count();
            totalBought += result.count();
        }

        return totalBought;
    }

    public int buyLandToMaintainUnused(KingdomEntity kingdom, int count)
    {
        var unusedLand = kingdom.getUnusedLand();
        assert unusedLand >= 0;
        var landBelowCap = gameConfig.bots().maxLand() - kingdom.getResources().getCount(ResourceName.land);
        if (unusedLand < count && landBelowCap > 0)
        {
            var action = new KingdomOtherAction(kingdom);
            return action.buyLand(Math.min(count, landBelowCap)).count();
        }

        return 0;
    }

    public void postOffer(KingdomEntity kingdom, IMarket market, MarketResource resource, int count, int price)
    {
        var countToOffer = Math.min(count, gameConfig.bots().getMaxOffer(resource));
        if (countToOffer > 0)
        {
            market.createOffer(kingdom.getName(), resource, countToOffer, price);
        }
    }

    /**
     * stores above the cap are shipped to distant markets, so a bot is never a bottomless prize for plunder
     */
    public static void shipSurplusAway(KingdomEntity kingdom, GameConfig gameConfig)
    {
        for (var resource : ResourceName.values())
        {
            var cap = gameConfig.bots().getMaxStock(resource);
            if (kingdom.getResources().getCount(resource) > cap)
            {
                kingdom.getResources().setCount(resource, cap);
            }
        }
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
        var kingdomBuildings = new KingdomBuildingsDto();
        kingdomBuildings.setCount(building, buildingsToBuild);

        var action = new KingdomBuildAction(kingdom, gameConfig);
        return action.build(kingdomBuildings).data().map(KingdomBuildingsDto::countAll).orElse(0);
    }

    public int trainUnits(KingdomEntity kingdom, UnitName unit, int count)
    {
        var toTrain = new UnitsMapDto();
        toTrain.setCount(unit, count);
        var action = new KingdomTrainAction(kingdom, gameConfig);
        var trainedUnits = action.train(toTrain);
        return trainedUnits.data().map(UnitsMapDto::countAll).orElse(0);
    }

    public int buyToolsToMaintainCount(IMarket market, int count)
    {
        var optionalOffer = market.getCheapestOfferByResource(MarketResource.tools);
        if (optionalOffer.isEmpty())
        {
            return 0;
        }

        var offer = optionalOffer.get();
        var result = BotTransactionalFunctions.buyOffer(market, count, offer, kingdom);
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
        toTrain.setCount(UnitName.builder, count);
        var action = new KingdomTrainAction(kingdom, gameConfig);
        var trainedUnits = action.train(toTrain);
        return trainedUnits.data().map(UnitsMapDto::countAll).orElse(0);
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

        var kingdomBuildings = new KingdomBuildingsDto();
        kingdomBuildings.setCount(BuildingName.house, count);

        var action = new KingdomBuildAction(kingdom, gameConfig);
        return action.build(kingdomBuildings).data().map(KingdomBuildingsDto::countAll).orElse(0);
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
        // TODO make it more sophisticated to withdraw only what is needed
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
