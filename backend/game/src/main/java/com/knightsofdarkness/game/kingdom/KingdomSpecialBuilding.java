package com.knightsofdarkness.game.kingdom;

import java.util.UUID;

public class KingdomSpecialBuilding {
    UUID id;
    SpecialBuildingType buildingType;
    int level;
    int buildingPointsPut;
    int buildingPointsRequired;
    boolean isMaxLevel;

    // TODO do something with this constructor, we want initialized objects here
    public KingdomSpecialBuilding(SpecialBuildingType specialBuilding)
    {
        id = UUID.randomUUID();
        buildingType = specialBuilding;
        level = 0;
        buildingPointsPut = 0;
        buildingPointsRequired = 10000; // TODO this will be variable
        isMaxLevel = false;
    }

    public KingdomSpecialBuilding(UUID id, SpecialBuildingType buildingType, int level, int buildingPointsPut, int buildingPointsRequired, boolean isMaxLevel)
    {
        this.id = id;
        this.buildingType = buildingType;
        this.level = level;
        this.buildingPointsPut = buildingPointsPut;
        this.buildingPointsRequired = buildingPointsRequired;
        this.isMaxLevel = isMaxLevel;
    }

    public UUID getId()
    {
        return id;
    }

    public SpecialBuildingType getBuildingType()
    {
        return buildingType;
    }

    public int getLevel()
    {
        return level;
    }

    public int getBuildingPointsPut()
    {
        return buildingPointsPut;
    }

    public int getBuildingPointsRequired()
    {
        return buildingPointsRequired;
    }

    public boolean isMaxLevel()
    {
        return isMaxLevel;
    }
}