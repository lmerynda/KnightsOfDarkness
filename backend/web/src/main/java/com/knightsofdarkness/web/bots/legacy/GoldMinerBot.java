package com.knightsofdarkness.web.bots.legacy;

import com.knightsofdarkness.common.kingdom.BuildingName;
import com.knightsofdarkness.common.kingdom.ResourceName;
import com.knightsofdarkness.common.kingdom.UnitName;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.kingdom.IKingdomInteractor;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;
import com.knightsofdarkness.web.kingdom.model.KingdomTurnAction;
import com.knightsofdarkness.web.market.IMarket;

public class GoldMinerBot extends Bot {
    private final IMarket market;
    private static final double builderToSpecialistRatio = 0.1;
    private static final double housesToSpecialistBuildingRatio = 0.6;
    private static final int weaponsProductionPercentage = 0;
    private IKingdomInteractor kingdomInteractor;

    public GoldMinerBot(KingdomEntity kingdom, IMarket market, IKingdomInteractor kingdomInteractor, GameConfig gameConfig)
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

        hasAnythingHappened += botFunctions.buyFoodForUpkeep(market);
        hasAnythingHappened += botFunctions.buyToolsToMaintainCount(market, kingdom, 5 * 15 + 20); // TODO calculate this from training cost configuration
        hasAnythingHappened += botFunctions.trainBuilders(kingdom, 1, builderToSpecialistRatio);
        hasAnythingHappened += botFunctions.trainUnits(kingdom, UnitName.goldMiner, 5);
        hasAnythingHappened += botFunctions.buyLandToMaintainUnused(kingdom, 2);
        hasAnythingHappened += botFunctions.buildSpecialistBuilding(kingdom, BuildingName.goldMine, 1);
        hasAnythingHappened += botFunctions.buildHouses(kingdom, 1, housesToSpecialistBuildingRatio);

        return hasAnythingHappened > 0;
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
        return String.format("[%s] land: %d, houses: %d, gold mines: %d, gold: %d, food: %d",
                kingdom.getName(),
                kingdom.getResources().getCount(ResourceName.land),
                kingdom.getBuildings().getCount(BuildingName.house),
                kingdom.getBuildings().getCount(BuildingName.goldMine),
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
