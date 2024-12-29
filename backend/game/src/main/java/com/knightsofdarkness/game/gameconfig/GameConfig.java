package com.knightsofdarkness.game.gameconfig;

public record GameConfig(
        BuildingPointCosts buildingPointCosts,
        BuildingCapacity buildingCapacity,
        TrainingCost trainingCost,
        Production production,
        KingdomStartConfiguration kingdomStartConfiguration,
        SpecialBuildingCosts specialBuildingCosts,
        MarketConfig market,
        Common common,
        CarrierCapacity carrierCapacity,
        Attack attack) {
}
