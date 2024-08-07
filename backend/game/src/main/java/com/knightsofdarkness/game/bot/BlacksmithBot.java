package com.knightsofdarkness.game.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knightsofdarkness.game.kingdom.BuildingName;
import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.game.kingdom.ResourceName;
import com.knightsofdarkness.game.kingdom.UnitName;
import com.knightsofdarkness.game.market.IMarket;
import com.knightsofdarkness.game.market.MarketResource;

public class BlacksmithBot implements IBot {
    private static final Logger log = LoggerFactory.getLogger(BlacksmithBot.class);
    private final Kingdom kingdom;
    private final IMarket market;
    private final double builderToSpecialistRatio = 0.07;
    private final double housesToSpecialistBuildingRatio = 0.6;

    public BlacksmithBot(Kingdom kingdom, IMarket market)
    {
        this.kingdom = kingdom;
        this.market = market;
    }

    @Override
    public boolean doUpkeepActions()
    {

        int actionResultsAggregate = 0;
        actionResultsAggregate += BotFunctions.buyFoodForUpkeep(kingdom, market);
        actionResultsAggregate += BotFunctions.buyEnoughIronToMaintainFullProduction(kingdom, market);

        return actionResultsAggregate > 0;
    }

    @Override
    public boolean doAllActions()
    {
        BotFunctions.withdrawAllOffers(kingdom, market);
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
        int actionResultsAggregate = 0;
        actionResultsAggregate += BotFunctions.buyFoodForUpkeep(kingdom, market);
        actionResultsAggregate += BotFunctions.buyEnoughIronToMaintainFullProduction(kingdom, market);
        actionResultsAggregate += BotFunctions.trainUnits(kingdom, UnitName.blacksmith, 3);
        actionResultsAggregate += BotFunctions.trainBuilders(kingdom, 1, builderToSpecialistRatio);
        actionResultsAggregate += BotFunctions.trainUnits(kingdom, UnitName.blacksmith, 2);
        actionResultsAggregate += BotFunctions.buyLandToMaintainUnused(kingdom, 2);
        actionResultsAggregate += BotFunctions.buildSpecialistBuilding(kingdom, BuildingName.workshop, 1);
        actionResultsAggregate += BotFunctions.buildHouses(kingdom, 1, housesToSpecialistBuildingRatio);
        actionResultsAggregate += BotFunctions.buyEnoughIronToMaintainFullProduction(kingdom, market);

        boolean hasAnythingHappen = actionResultsAggregate > 0;
        return hasAnythingHappen;
    }

    private int postToolsOffer(double percentage)
    {
        var toolsAmount = kingdom.getResources().getCount(ResourceName.tools);

        if (toolsAmount > 0)
        {
            market.addOffer(kingdom, MarketResource.tools, (int) (toolsAmount * percentage), 140);
        }

        return toolsAmount;
    }

    @Override
    public void passTurn()
    {
        runPrePassTurnActions();
        kingdom.passTurn();
    }

    private void runPrePassTurnActions()
    {
        int buildingPointsSpent = 0;
        do
        {
            buildingPointsSpent = BotFunctions.putRemainingPointsToLowestLevelSpecialBuilding(kingdom);
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
    public Kingdom getKingdom()
    {
        return kingdom;
    }

    @Override
    public boolean doesHaveEnoughUpkeep()
    {
        var doesHaveEnoughFood = BotFunctions.doesHaveEnoughFoodForNextTurn(kingdom);
        var doesHaveEnoughIron = doesHaveEnoughIron();
        return doesHaveEnoughFood && doesHaveEnoughIron;
    }

    private boolean doesHaveEnoughIron()
    {
        var nourishmentFactor = 1.0; // Assume everyone is fed
        var ironUpkeep = kingdom.getIronUpkeep(nourishmentFactor);
        var ironAmount = kingdom.getResources().getCount(ResourceName.iron);
        double ironReserve = (double)ironAmount / ironUpkeep;
        log.info("[{}] iron reserve {}", kingdom.getName(), ironReserve);
        return ironReserve >= 0.8d;
    }
}
