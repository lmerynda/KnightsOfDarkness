package com.knightsofdarkness.common.kingdom;

import com.knightsofdarkness.game.kingdom.SpecialBuildingType;

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
