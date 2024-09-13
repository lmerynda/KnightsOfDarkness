package com.knightsofdarkness.game.bot;

import com.knightsofdarkness.common.market.MarketResource;
import com.knightsofdarkness.game.kingdom.BuildingName;
import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.game.kingdom.ResourceName;
import com.knightsofdarkness.game.kingdom.UnitName;
import com.knightsofdarkness.game.market.IMarket;

public class IronMinerBot implements IBot {
    private static final double builderToSpecialistRatio = 0.1;
    private static final double housesToSpecialistBuildingRatio = 0.6;
    private final Kingdom kingdom;
    private final IMarket market;

    public IronMinerBot(Kingdom kingdom, IMarket market)
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

        postIronOffer();

        return hasAnythingHappened;
    }

    @Override
    public boolean doActionCycle()
    {
        var hasAnythingHappened = 0;
        hasAnythingHappened += BotFunctions.buyFoodForUpkeep(kingdom, market);
        hasAnythingHappened += BotFunctions.buyToolsToMaintainCount(market, kingdom, 5 * 15 + 20); // TODO calculate this from training cost configuration
        hasAnythingHappened += BotFunctions.trainBuilders(kingdom, 1, builderToSpecialistRatio);
        hasAnythingHappened += BotFunctions.trainUnits(kingdom, UnitName.ironMiner, 5);
        hasAnythingHappened += BotFunctions.buyLandToMaintainUnused(kingdom, 2);
        hasAnythingHappened += BotFunctions.buildSpecialistBuilding(kingdom, BuildingName.ironMine, 1);
        hasAnythingHappened += BotFunctions.buildHouses(kingdom, 1, housesToSpecialistBuildingRatio);

        return hasAnythingHappened > 0;
    }

    private int postIronOffer()
    {
        var ironAmount = kingdom.getResources().getCount(ResourceName.iron);

        if (ironAmount > 0)
        {
            market.createOffer(kingdom.getName(), MarketResource.iron, ironAmount, 25);
        }

        return ironAmount;
    }

    @Override
    public void passTurn()
    {
        runPrePassTurnActions();
        kingdom.passTurn();
    }

    // TODO time to have a base bot class?
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
        return String.format("[%s] land: %d, houses: %d, iron mines: %d, gold: %d, food: %d", kingdom.getName(), kingdom.getResources().getCount(ResourceName.land), kingdom.getBuildings().getCount(BuildingName.house),
                kingdom.getBuildings().getCount(BuildingName.ironMine), kingdom.getResources().getCount(ResourceName.gold), kingdom.getResources().getCount(ResourceName.food));
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
