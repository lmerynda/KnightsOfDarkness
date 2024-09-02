package com.knightsofdarkness.game.gameconfig;
// @formatter:off 

public record GameConfig(
        BuildingPointCosts buildingPointCosts,
        BuildingCapacity buildingCapacity,
        TrainingCost trainingCost,
        Production production,
        KingdomStartConfiguration kingdomStartConfiguration,
        SpecialBuildingCosts specialBuildingCosts,
        Market market)
{
}
// @formatter:on 