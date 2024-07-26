package com.knightsofdarkness.game.bot;

import com.knightsofdarkness.game.kingdom.BuildingName;
import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.game.kingdom.ResourceName;
import com.knightsofdarkness.game.kingdom.UnitName;
import com.knightsofdarkness.game.market.IMarket;
import com.knightsofdarkness.game.market.MarketResource;

public class BlacksmithBot implements Bot {
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
        int actionResultsAggregate = 0;
        actionResultsAggregate += BotFunctions.buyFoodForUpkeep(kingdom, market);
        actionResultsAggregate += BotFunctions.buyEnoughIronToMaintainFullProduction(kingdom, market);
        withdrawToolsOffer();
        actionResultsAggregate += BotFunctions.trainUnits(kingdom, UnitName.blacksmith, 3);
        actionResultsAggregate += BotFunctions.trainBuilders(kingdom, 1, builderToSpecialistRatio);
        actionResultsAggregate += BotFunctions.trainUnits(kingdom, UnitName.blacksmith, 2);
        actionResultsAggregate += BotFunctions.buyLandToMaintainUnused(kingdom, 2);
        actionResultsAggregate += BotFunctions.buildSpecialistBuilding(kingdom, BuildingName.workshop, 1);
        actionResultsAggregate += BotFunctions.buildHouses(kingdom, 1, housesToSpecialistBuildingRatio);
        postToolsOffer();
        actionResultsAggregate += BotFunctions.buyEnoughIronToMaintainFullProduction(kingdom, market);

        boolean hasAnythingHappen = actionResultsAggregate > 0;
        return hasAnythingHappen;
    }

    private int withdrawToolsOffer()
    {
        var kingdomOffers = market.getOffersByKingdomName(kingdom.getName());
        // TODO why count kingdomOffers?
        var count = kingdomOffers.size();
        for (var offer : kingdomOffers)
        {
            market.removeOffer(offer);
        }

        return count;
    }

    private int postToolsOffer()
    {
        var toolsAmount = kingdom.getResources().getCount(ResourceName.tools);

        if (toolsAmount > 0)
        {
            market.addOffer(kingdom, MarketResource.tools, toolsAmount, 140);
        }

        return toolsAmount;
    }

    @Override
    public void passTurn()
    {
        kingdom.passTurn();
    }

    @Override
    public String getKingdomInfo()
    {
        return String.format("[%s] passed turn, land: %d, houses: %d, workshops: %d, gold: %d, food: %d", kingdom.getName(), kingdom.getResources().getCount(ResourceName.land), kingdom.getBuildings().getCount(BuildingName.house),
                kingdom.getBuildings().getCount(BuildingName.workshop), kingdom.getResources().getCount(ResourceName.gold), kingdom.getResources().getCount(ResourceName.food));
    }

    @Override
    public Kingdom getKingdom()
    {
        return kingdom;
    }
}
