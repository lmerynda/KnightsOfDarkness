package com.knightsofdarkness.web.common.kingdom;

import java.util.Map;

/**
 * Numbers derived from the kingdom state by the game rules, so clients don't have to re-implement them.
 * The forecast is the report the next turn would produce if nothing changes before it.
 */
public record KingdomDetailsDto(
        int usedLand,
        int housingCapacity,
        int housedPeople,
        Map<BuildingName, Integer> buildingCapacity,
        Map<BuildingName, Integer> buildingOccupancy,
        int foodUpkeep,
        double defensePower,
        double counterIntelligence,
        int nextLandPrice,
        KingdomTurnReport forecast) {
}
