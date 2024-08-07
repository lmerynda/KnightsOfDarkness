package com.knightsofdarkness.common;

import java.util.UUID;

import com.knightsofdarkness.game.kingdom.KingdomSpecialBuilding;
import com.knightsofdarkness.game.kingdom.SpecialBuildingType;

public class KingdomSpecialBuildingDto {
    public UUID id;
    public SpecialBuildingType buildingType;
    public int level;
    public int buildingPointsPut;
    public int buildingPointsRequired;
    public boolean isMaxLevel;

    public KingdomSpecialBuildingDto(UUID id, SpecialBuildingType buildingType, int level, int buildingPointsPut, int buildingPointsRequired, boolean isMaxLevel)
    {
        this.id = id;
        this.buildingType = buildingType;
        this.level = level;
        this.buildingPointsPut = buildingPointsPut;
        this.buildingPointsRequired = buildingPointsRequired;
        this.isMaxLevel = isMaxLevel;
    }

    public static KingdomSpecialBuildingDto fromDomain(KingdomSpecialBuilding specialBuilding)
    {
        return new KingdomSpecialBuildingDto(specialBuilding.getId(), specialBuilding.getBuildingType(), specialBuilding.getLevel(), specialBuilding.getBuildingPointsPut(), specialBuilding.getBuildingPointsRequired(),
                specialBuilding.isMaxLevel());
    }

    public KingdomSpecialBuilding toDomain()
    {
        return new KingdomSpecialBuilding(id, buildingType, level, buildingPointsPut, buildingPointsRequired, isMaxLevel);
    }

}
