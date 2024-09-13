package com.knightsofdarkness.common.kingdom;

public enum SpecialBuildingType
{
    goldShaft, ironShaft, granary, forge, warehouse, emptyBuilding;

    public static SpecialBuildingType fromResource(ResourceName resourceName)
    {
        return switch (resourceName)
        {
            case gold -> goldShaft;
            case iron -> ironShaft;
            case food -> granary;
            case tools, weapons -> forge;
            default -> emptyBuilding;
        };
    }

}
