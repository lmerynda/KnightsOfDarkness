package com.knightsofdarkness.web.bots.legacy;

import com.knightsofdarkness.common.kingdom.BuildingName;
import com.knightsofdarkness.common.kingdom.ResourceName;
import com.knightsofdarkness.common.kingdom.UnitName;
import com.knightsofdarkness.web.kingdom.IKingdomInteractor;
import com.knightsofdarkness.web.kingdom.legacy.Kingdom;
import com.knightsofdarkness.web.market.IMarket;

public class GoldMinerBot implements IBot {
    private final Kingdom kingdom;
    private final IMarket market;
    private static final double builderToSpecialistRatio = 0.1;
    private static final double housesToSpecialistBuildingRatio = 0.6;
    private static final int weaponsProductionPercentage = 0;
    private IKingdomInteractor kingdomInteractor;

    public GoldMinerBot(Kingdom kingdom, IMarket market, IKingdomInteractor kingdomInteractor)
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
        boolean hasAnythingHappened = true;
        do
        {
            hasAnythingHappened = doActionCycle();
        } while (hasAnythingHappened);

        return hasAnythingHappened;
    }

    @Override
    public boolean doActionCycle()
    {
        var hasAnythingHappened = 0;

        hasAnythingHappened += BotFunctions.buyFoodForUpkeep(kingdom, market);
        hasAnythingHappened += BotFunctions.buyToolsToMaintainCount(market, kingdom, 5 * 15 + 20); // TODO calculate this from training cost configuration
        hasAnythingHappened += BotFunctions.trainBuilders(kingdom, 1, builderToSpecialistRatio);
        hasAnythingHappened += BotFunctions.trainUnits(kingdom, UnitName.goldMiner, 5);
        hasAnythingHappened += BotFunctions.buyLandToMaintainUnused(kingdom, 2);
        hasAnythingHappened += BotFunctions.buildSpecialistBuilding(kingdom, BuildingName.goldMine, 1);
        hasAnythingHappened += BotFunctions.buildHouses(kingdom, 1, housesToSpecialistBuildingRatio);

        return hasAnythingHappened > 0;
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
        return String.format("[%s] land: %d, houses: %d, gold mines: %d, gold: %d, food: %d",
                kingdom.getName(),
                kingdom.getResources().getCount(ResourceName.land),
                kingdom.getBuildings().getCount(BuildingName.house),
                kingdom.getBuildings().getCount(BuildingName.goldMine),
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
