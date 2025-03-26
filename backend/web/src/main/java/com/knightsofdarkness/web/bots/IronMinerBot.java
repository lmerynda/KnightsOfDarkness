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

public class IronMinerBot extends Bot {
    private static final double builderToSpecialistRatio = 0.1;
    private static final double housesToSpecialistBuildingRatio = 0.6;
    private static final int weaponsProductionPercentage = 0;
    private final IMarket market;
    private final IKingdomInteractor kingdomInteractor;

    public IronMinerBot(KingdomEntity kingdom, IMarket market, IKingdomInteractor kingdomInteractor, GameConfig gameConfig)
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

        postIronOffer();

        return hasAnythingHappened;
    }

    @Override
    public boolean doActionCycle()
    {
        var hasAnythingHappened = 0;
        hasAnythingHappened += botFunctions.buyFoodForUpkeep(market);
        hasAnythingHappened += botFunctions.buyToolsToMaintainCount(market, kingdom, 5 * 15 + 20); // TODO calculate this from training cost configuration
        hasAnythingHappened += botFunctions.trainBuilders(kingdom, 1, builderToSpecialistRatio);
        hasAnythingHappened += botFunctions.trainUnits(kingdom, UnitName.ironMiner, 5);
        hasAnythingHappened += botFunctions.buyLandToMaintainUnused(kingdom, 2);
        hasAnythingHappened += botFunctions.buildSpecialistBuilding(kingdom, BuildingName.ironMine, 1);
        hasAnythingHappened += botFunctions.buildHouses(kingdom, 1, housesToSpecialistBuildingRatio);

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
        var action = new KingdomTurnAction(kingdom, kingdomInteractor, gameConfig, kingdomDetailsProvider);
        action.passTurn(weaponsProductionPercentage);
    }

    // TODO time to have a base bot class?
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
        return String.format("[%s] land: %d, houses: %d, iron mines: %d, gold: %d, food: %d", kingdom.getName(), kingdom.getResources().getCount(ResourceName.land), kingdom.getBuildings().getCount(BuildingName.house),
                kingdom.getBuildings().getCount(BuildingName.ironMine), kingdom.getResources().getCount(ResourceName.gold), kingdom.getResources().getCount(ResourceName.food));
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
