package com.knightsofdarkness.web.common.kingdom;

import java.util.UUID;

public record KingdomSpecialBuildingDto(UUID id, SpecialBuildingType buildingType, int level, int buildingPointsPut, int buildingPointsRequired, boolean isMaxLevel) {
}
