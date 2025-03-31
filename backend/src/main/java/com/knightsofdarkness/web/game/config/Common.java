package com.knightsofdarkness.web.game.config;

public record Common(
        double specialBuildingPerLevelProductionBonus,
        int maxTurns,
        int foodUpkeepPerUnit,
        int ironConsumptionPerProductionUnit,
        int specialBuildingMaxCount,
        int newTurnPeriodicity,
        int maxNotificationsCount,
        int turnsToDeliverResources,
        int kingdomBaseHousingCapacity,
        int allianceMaxMembers) {
}
