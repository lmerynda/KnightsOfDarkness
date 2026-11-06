package com.knightsofdarkness.web.game.config;

public record Common(
        double specialBuildingPerLevelProductionBonus,
        int maxTurns,
        int foodUpkeepPerUnit,
        int ironConsumptionPerProductionUnit,
        int specialBuildingMaxCount,
        int specialBuildingMaxLevel,
        double starvationLeaveRatio,
        int newTurnPeriodicity,
        int maxNotificationsCount,
        int turnsToDeliverResources,
        int kingdomBaseHousingCapacity,
        int allianceMaxMembers,
        int turnsToReinforce,
        double soldierUpkeepRatio,
        /*
         * the extra production share of a kingdom built on 100 acres or fewer, fading to none at 1000 acres: 4 makes a
         * small kingdom produce five times as much
         */
        double smallKingdomProductionBonus) {
}
