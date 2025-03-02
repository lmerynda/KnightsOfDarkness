package com.knightsofdarkness.web.bots.legacy;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knightsofdarkness.common.kingdom.BuildingName;
import com.knightsofdarkness.common.kingdom.ResourceName;
import com.knightsofdarkness.common.kingdom.UnitName;
import com.knightsofdarkness.common.market.MarketResource;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.kingdom.IKingdomInteractor;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;
import com.knightsofdarkness.web.kingdom.model.KingdomTurnAction;
import com.knightsofdarkness.web.market.IMarket;

public class BlacksmithBot extends Bot {
    private static final Logger log = LoggerFactory.getLogger(BlacksmithBot.class);
    private static final double builderToSpecialistRatio = 0.07;
    private static final double housesToSpecialistBuildingRatio = 0.6;
    private static final double toolsPriceIfUnknown = 140.0;
    private static final int minimumMarketPrice = 5;
    private static final int weaponsProductionPercentage = 0;
    private final IMarket market;
    private final Random random = new Random();
    private IKingdomInteractor kingdomInteractor;

    public BlacksmithBot(KingdomEntity kingdom, IMarket market, IKingdomInteractor kingdomInteractor, GameConfig gameConfig)
    {
        super(kingdom, gameConfig);
        this.market = market;
        this.kingdomInteractor = kingdomInteractor;
    }

    @Override
    public boolean doUpkeepActions()
    {
        int actionResultsAggregate = 0;
        actionResultsAggregate += botFunctions.buyFoodForUpkeep(market);
        actionResultsAggregate += botFunctions.buyEnoughIronToMaintainFullProduction(kingdom, market);

        return actionResultsAggregate > 0;
    }

    @Override
    public boolean doAllActions()
    {
        botFunctions.withdrawAllOffers(kingdom, market);
        // always put something on the market to have any income for supplies
        postToolsOffer(0.2);
        doUpkeepActions();

        boolean hasAnythingHappened = true;

        if (doesHaveEnoughUpkeep())
        {
            do
            {
                hasAnythingHappened = doActionCycle();
            } while (hasAnythingHappened);
            postToolsOffer(1.0);
        }

        return hasAnythingHappened;
    }

    @Override
    public boolean doActionCycle()
    {
        var hasAnythingHappened = 0;
        hasAnythingHappened += botFunctions.buyFoodForUpkeep(market);
        hasAnythingHappened += botFunctions.buyEnoughIronToMaintainFullProduction(kingdom, market);
        hasAnythingHappened += botFunctions.trainUnits(kingdom, UnitName.blacksmith, 3);
        hasAnythingHappened += botFunctions.trainBuilders(kingdom, 1, builderToSpecialistRatio);
        hasAnythingHappened += botFunctions.trainUnits(kingdom, UnitName.blacksmith, 2);
        hasAnythingHappened += botFunctions.buyLandToMaintainUnused(kingdom, 2);
        hasAnythingHappened += botFunctions.buildSpecialistBuilding(kingdom, BuildingName.workshop, 1);
        hasAnythingHappened += botFunctions.buildHouses(kingdom, 1, housesToSpecialistBuildingRatio);
        hasAnythingHappened += botFunctions.buyEnoughIronToMaintainFullProduction(kingdom, market);

        return hasAnythingHappened > 0;
    }

    private int postToolsOffer(double percentage)
    {
        var toolsAmount = kingdom.getResources().getCount(ResourceName.tools);

        if (toolsAmount > 0)
        {
            market.createOffer(kingdom.getName(), MarketResource.tools, (int) (toolsAmount * percentage), runPricingAlgorithm());
        }

        return toolsAmount;
    }

    private int runPricingAlgorithm()
    {
        double average = market.getLast24TransactionAverages(MarketResource.tools).orElse(toolsPriceIfUnknown);
        // final price is a average * random int [0.9,1.1]
        int finalPrice = (int) Math.round(average * (0.9 + random.nextInt(21) / 100.0));
        finalPrice = Math.max(finalPrice, minimumMarketPrice);
        return finalPrice;
    }

    @Override
    public void passTurn()
    {
        if (!shouldPassTurn())
        {
            log.info("[{}] didn't have at least 80% upkeep for turn pass, skipping", kingdom.getName());
            return;
        }

        runPrePassTurnActions();
        var action = new KingdomTurnAction(kingdom, kingdomInteractor, gameConfig, kingdomDetailsProvider);
        log.info("[BlacksmithBot] transaction average for last 24 entires for tools is {}", market.getLast24TransactionAverages(MarketResource.tools));
        action.passTurn(weaponsProductionPercentage);
    }

    private void runPrePassTurnActions()
    {
        int buildingPointsSpent = 0;
        do
        {
            buildingPointsSpent = botFunctions.putRemainingPointsToLowestLevelSpecialBuilding(kingdom);
        } while (buildingPointsSpent > 0);
    }

    @Override
    public String getKingdomInfo()
    {
        return String.format("[%s] land: %d, houses: %d, workshops: %d, gold: %d, food: %d, iron: %d, tools: %d", kingdom.getName(), kingdom.getResources().getCount(ResourceName.land),
                kingdom.getBuildings().getCount(BuildingName.house),
                kingdom.getBuildings().getCount(BuildingName.workshop), kingdom.getResources().getCount(ResourceName.gold), kingdom.getResources().getCount(ResourceName.food), kingdom.getResources().getCount(ResourceName.iron),
                kingdom.getResources().getCount(ResourceName.tools));
    }

    @Override
    public KingdomEntity getKingdom()
    {
        return kingdom;
    }

    @Override
    public boolean doesHaveEnoughUpkeep()
    {
        var doesHaveEnoughFood = botFunctions.doesHaveEnoughFoodForNextTurn(kingdom);
        var doesHaveEnoughIron = doesHaveEnoughIron();
        return doesHaveEnoughFood && doesHaveEnoughIron;
    }

    private boolean doesHaveEnoughIron()
    {
        var nourishmentFactor = 1.0; // Assume everyone is fed
        var ironUpkeep = kingdomDetailsProvider.getIronUpkeep(kingdom, nourishmentFactor);
        var ironAmount = kingdom.getResources().getCount(ResourceName.iron);
        double ironReserve = (double) ironAmount / ironUpkeep;
        log.info("[{}] iron reserve {}", kingdom.getName(), ironReserve);
        return ironReserve >= 0.8d;
    }
}
