package com.merynda.kod.gameconfig;
// @formatter:off 

public record GameConfig(
        BuildingPointCosts buildingPointCosts,
        BuildingCapacity buildingCapacity,
        TrainingCost trainingCost,
        Production production,
        KingdomStartConfiguration kingdomStartConfiguration,
        SpecialBuildingCosts specialBuildingCosts)
{
}
// @formatter:on 