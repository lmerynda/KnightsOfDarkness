package com.merynda.kod.bot;

import com.merynda.kod.kingdom.BuildingName;
import com.merynda.kod.kingdom.Kingdom;
import com.merynda.kod.kingdom.ResourceName;
import com.merynda.kod.kingdom.UnitName;
import com.merynda.kod.market.MarketResource;

public class IronMinerBot implements Bot {
    private final Kingdom kingdom;
    private final double builderToSpecialistRatio = 0.15;
    private final double housesToSpecialistBuildingRatio = 0.6;

    public IronMinerBot(Kingdom kingdom)
    {
        this.kingdom = kingdom;
    }

    @Override
    public boolean doAllActions()
    {
        int actionResultsAggregate = 0;
        actionResultsAggregate += BotFunctions.buyFoodForUpkeep(kingdom);
        actionResultsAggregate += BotFunctions.buyToolsToMaintainCount(kingdom, 5 * 15 + 20); // TODO calculate this from training cost configuration
        actionResultsAggregate += BotFunctions.trainBuilders(kingdom, 1, builderToSpecialistRatio);
        actionResultsAggregate += BotFunctions.trainUnits(kingdom, UnitName.ironMiner, 5);
        actionResultsAggregate += BotFunctions.buyLandToMaintainUnused(kingdom, 2);
        actionResultsAggregate += BotFunctions.buildSpecialistBuilding(kingdom, BuildingName.ironMine, 1);
        actionResultsAggregate += BotFunctions.buildHouses(kingdom, 1, housesToSpecialistBuildingRatio);
        actionResultsAggregate += postIronOffer();

        boolean hasAnythingHappen = actionResultsAggregate > 0;
        return hasAnythingHappen;
    }

    private int postIronOffer()
    {
        var ironAmount = kingdom.getResources().getCount(ResourceName.iron);

        if (ironAmount > 0)
        {
            kingdom.postMarketOffer(MarketResource.iron, ironAmount, 15);
        }

        return ironAmount;
    }

    @Override
    public void passTurn()
    {
        kingdom.passTurn();
    }

    @Override
    public String getKingdomInfo()
    {
        return String.format("[%s] passed turn, land: %d, houses: %d, iron mines: %d, gold: %d, food: %d", kingdom.getName(), kingdom.getResources().getCount(ResourceName.land), kingdom.getBuildings().getCount(BuildingName.house),
                kingdom.getBuildings().getCount(BuildingName.ironMine), kingdom.getResources().getCount(ResourceName.gold), kingdom.getResources().getCount(ResourceName.food));
    }

    @Override
    public Kingdom getKingdom()
    {
        return kingdom;
    }
}
