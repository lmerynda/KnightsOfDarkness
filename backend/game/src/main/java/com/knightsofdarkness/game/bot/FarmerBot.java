package com.knightsofdarkness.game.bot;

import com.knightsofdarkness.game.kingdom.BuildingName;
import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.game.kingdom.ResourceName;
import com.knightsofdarkness.game.kingdom.UnitName;
import com.knightsofdarkness.game.market.IMarket;
import com.knightsofdarkness.game.market.MarketResource;

public class FarmerBot implements IBot {
    private final Kingdom kingdom;
    private final IMarket market;
    private final double builderToSpecialistRatio = 0.05;
    private final double housesToSpecialistBuildingRatio = 0.55;

    public FarmerBot(Kingdom kingdom, IMarket market)
    {
        this.kingdom = kingdom;
        this.market = market;
    }

    @Override
    public boolean doUpkeepActions()
    {
        int actionResult = BotFunctions.buyFoodForUpkeep(kingdom, market);

        return actionResult > 0;
    }

    @Override
    public boolean doAllActions()
    {
        BotFunctions.withdrawAllOffers(kingdom, market);

        boolean hasAnythingHappened = true;
        do
        {
            hasAnythingHappened = doActionCycle();
        } while (hasAnythingHappened);

        postFoodOffer();

        return hasAnythingHappened;
    }

    @Override
    public boolean doActionCycle()
    {
        int actionResultsAggregate = 0;

        actionResultsAggregate += BotFunctions.buyToolsToMaintainCount(market, kingdom, 5 * 5 + 20);
        // TODO calculate this from training cost configuration
        actionResultsAggregate += BotFunctions.trainBuilders(kingdom, 1, builderToSpecialistRatio);
        actionResultsAggregate += BotFunctions.trainUnits(kingdom, UnitName.farmer, 5);
        actionResultsAggregate += BotFunctions.buyLandToMaintainUnused(kingdom, 2);
        actionResultsAggregate += BotFunctions.buildSpecialistBuilding(kingdom, BuildingName.farm, 1);
        actionResultsAggregate += BotFunctions.buildHouses(kingdom, 1, housesToSpecialistBuildingRatio);

        boolean hasAnythingHappen = actionResultsAggregate > 0;
        return hasAnythingHappen;
    }

    private int postFoodOffer()
    {
        var foodAmount = kingdom.getResources().getCount(ResourceName.food);
        var foodUpkeep = kingdom.getFoodUpkeep();
        var amountToOffer = Math.max(0, foodAmount - foodUpkeep);

        if (amountToOffer > 0)
        {
            market.addOffer(kingdom, MarketResource.food, amountToOffer, 15);
        }

        return amountToOffer;
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
        return String.format("[%s] land: %d, houses: %d, farms: %d, gold: %d, food: %d",
                kingdom.getName(),
                kingdom.getResources().getCount(ResourceName.land),
                kingdom.getBuildings().getCount(BuildingName.house),
                kingdom.getBuildings().getCount(BuildingName.farm),
                kingdom.getResources().getCount(ResourceName.gold),
                kingdom.getResources().getCount(ResourceName.food));
    }

    @Override
    public Kingdom getKingdom()
    {
        return kingdom;
    }

    @Override
    public boolean doesHaveEnoughUpkeep()
    {
        return BotFunctions.doesHaveEnoughFoodForNextTurn(kingdom);
    }
}
