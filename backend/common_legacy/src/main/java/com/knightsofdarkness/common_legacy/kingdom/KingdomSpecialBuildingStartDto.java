package com.knightsofdarkness.common_legacy.kingdom;

import com.knightsofdarkness.common.kingdom.SpecialBuildingType;

public class KingdomSpecialBuildingStartDto {
    public SpecialBuildingType name;

    public KingdomSpecialBuildingStartDto()
    {
    }

    public KingdomSpecialBuildingStartDto(SpecialBuildingType name)
    {
        this.name = name;
    }

    public String toString()
    {
        return "KingdomSpecialBuildingStartDto{" +
                "name=" + name +
                '}';
    }
}
