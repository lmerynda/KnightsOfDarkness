package com.knightsofdarkness.common;

import com.knightsofdarkness.game.kingdom.SpecialBuildingType;

public class KingdomSpecialBuildingStartDto {
    public SpecialBuildingType name;

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
