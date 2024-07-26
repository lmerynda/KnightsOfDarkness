package com.knightsofdarkness.game.bot;

import com.knightsofdarkness.game.kingdom.BuildingName;
import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.game.kingdom.ResourceName;
import com.knightsofdarkness.game.kingdom.UnitName;
import com.knightsofdarkness.game.market.IMarket;
import com.knightsofdarkness.game.market.MarketResource;

public class IronMinerBot implements Bot {
    private final Kingdom kingdom;
    private final IMarket market;
    private final double builderToSpecialistRatio = 0.15;
    private final double housesToSpecialistBuildingRatio = 0.6;

    public IronMinerBot(Kingdom kingdom, IMarket market)
    {
        this.kingdom = kingdom;
        this.market = market;
    }

    @Override
    public boolean doAllActions() {
        return false;
    }

    @Override
    public boolean doActionCycle()
    {
        int actionResultsAggregate = 0;
        actionResultsAggregate += BotFunctions.buyFoodForUpkeep(kingdom, market);
        actionResultsAggregate += BotFunctions.buyToolsToMaintainCount(market, kingdom, 5 * 15 + 20); // TODO calculate this from training cost configuration
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
            market.addOffer(kingdom, MarketResource.iron, ironAmount, 15);
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
