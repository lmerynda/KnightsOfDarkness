package com.knightsofdarkness.game.bot;

import com.knightsofdarkness.common.kingdom.BuildingName;
import com.knightsofdarkness.common.kingdom.ResourceName;
import com.knightsofdarkness.common.kingdom.UnitName;
import com.knightsofdarkness.common.market.MarketResource;
import com.knightsofdarkness.game.interactions.IKingdomInteractor;
import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.game.market.IMarket;

public class FarmerBot implements IBot {
    private static final double builderToSpecialistRatio = 0.05;
    private static final double housesToSpecialistBuildingRatio = 0.55;
    private static final int weaponsProductionPercentage = 0;
    private final Kingdom kingdom;
    private final IMarket market;
    private final IKingdomInteractor kingdomInteractor;

    public FarmerBot(Kingdom kingdom, IMarket market, IKingdomInteractor kingdomInteractor)
    {
        this.kingdom = kingdom;
        this.market = market;
        this.kingdomInteractor = kingdomInteractor;
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
        var hasAnythingHappened = 0;

        hasAnythingHappened += BotFunctions.buyToolsToMaintainCount(market, kingdom, 5 * 5 + 20);
        // TODO calculate this from training cost configuration
        hasAnythingHappened += BotFunctions.trainBuilders(kingdom, 1, builderToSpecialistRatio);
        hasAnythingHappened += BotFunctions.trainUnits(kingdom, UnitName.farmer, 5);
        hasAnythingHappened += BotFunctions.buyLandToMaintainUnused(kingdom, 2);
        hasAnythingHappened += BotFunctions.buildSpecialistBuilding(kingdom, BuildingName.farm, 1);
        hasAnythingHappened += BotFunctions.buildHouses(kingdom, 1, housesToSpecialistBuildingRatio);

        return hasAnythingHappened > 0;
    }

    private int postFoodOffer()
    {
        var foodAmount = kingdom.getResources().getCount(ResourceName.food);
        var foodUpkeep = kingdom.getFoodUpkeep();
        var amountToOffer = Math.max(0, foodAmount - foodUpkeep);

        if (amountToOffer > 0)
        {
            market.createOffer(kingdom.getName(), MarketResource.food, amountToOffer, 15);
        }

        return amountToOffer;
    }

    @Override
    public void passTurn()
    {
        runPrePassTurnActions();
        kingdom.passTurn(kingdomInteractor, weaponsProductionPercentage);
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
