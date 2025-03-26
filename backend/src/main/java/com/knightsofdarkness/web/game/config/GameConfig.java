package com.knightsofdarkness.web.game.config;

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
