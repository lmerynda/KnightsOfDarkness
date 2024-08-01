package com.knightsofdarkness.common;

import com.knightsofdarkness.game.kingdom.SpecialBuildingType;

public class KingdomSpecialBuildingBuildDto {
    public int buildingPlace;
    public SpecialBuildingType specialBuilding;

    public KingdomSpecialBuildingBuildDto()
    {
    }

    public KingdomSpecialBuildingBuildDto(int buildingPlace, SpecialBuildingType specialBuilding)
    {
        this.buildingPlace = buildingPlace;
        this.specialBuilding = specialBuilding;
    }

    public String toString()
    {
        return "KingdomSpecialBuildingBuildDto{" +
                "buildingPlace=" + buildingPlace +
                ", specialBuilding=" + specialBuilding +
                '}';
    }
}
