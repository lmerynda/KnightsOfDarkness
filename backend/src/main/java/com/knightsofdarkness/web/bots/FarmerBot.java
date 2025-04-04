package com.knightsofdarkness.web.bots;

import com.knightsofdarkness.web.common.kingdom.BuildingName;
import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.kingdom.UnitName;
import com.knightsofdarkness.web.common.market.MarketResource;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.kingdom.IKingdomInteractor;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;
import com.knightsofdarkness.web.kingdom.model.KingdomTurnAction;
import com.knightsofdarkness.web.market.IMarket;

public class FarmerBot extends Bot
{
    private static final double builderToSpecialistRatio = 0.05;
    private static final double housesToSpecialistBuildingRatio = 0.55;
    private static final int weaponsProductionPercentage = 0;
    private final IMarket market;
    private final IKingdomInteractor kingdomInteractor;

    public FarmerBot(KingdomEntity kingdom, IMarket market, IKingdomInteractor kingdomInteractor, GameConfig gameConfig)
    {
        super(kingdom, gameConfig);
        this.market = market;
        this.kingdomInteractor = kingdomInteractor;
    }

    @Override
    public boolean doUpkeepActions()
    {
        int actionResult = botFunctions.buyFoodForUpkeep(market);

        return actionResult > 0;
    }

    @Override
    public boolean doAllActions()
    {
        botFunctions.withdrawAllOffers(kingdom, market);

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

        hasAnythingHappened += botFunctions.buyToolsToMaintainCount(market, 5 * 5 + 20);
        // TODO calculate this from training cost configuration
        hasAnythingHappened += botFunctions.trainBuilders(kingdom, 1, builderToSpecialistRatio);
        hasAnythingHappened += botFunctions.trainUnits(kingdom, UnitName.farmer, 5);
        hasAnythingHappened += botFunctions.buyLandToMaintainUnused(kingdom, 2);
        hasAnythingHappened += botFunctions.buildSpecialistBuilding(kingdom, BuildingName.farm, 1);
        hasAnythingHappened += botFunctions.buildHouses(kingdom, 1, housesToSpecialistBuildingRatio);

        return hasAnythingHappened > 0;
    }

    private int postFoodOffer()
    {
        var foodAmount = kingdom.getResources().getCount(ResourceName.food);
        var foodUpkeep = kingdomDetailsProvider.getFoodUpkeep(kingdom);
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
        var action = new KingdomTurnAction(kingdom, kingdomInteractor, gameConfig, kingdomDetailsProvider);
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
        return String.format("[%s] land: %d, houses: %d, farms: %d, gold: %d, food: %d",
                kingdom.getName(),
                kingdom.getResources().getCount(ResourceName.land),
                kingdom.getBuildings().getCount(BuildingName.house),
                kingdom.getBuildings().getCount(BuildingName.farm),
                kingdom.getResources().getCount(ResourceName.gold),
                kingdom.getResources().getCount(ResourceName.food));
    }

    @Override
    public KingdomEntity getKingdom()
    {
        return kingdom;
    }

    @Override
    public boolean doesHaveEnoughUpkeep()
    {
        return botFunctions.doesHaveEnoughFoodForNextTurn(kingdom);
    }
}
