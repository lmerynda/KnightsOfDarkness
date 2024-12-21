package com.knightsofdarkness.common.kingdom;

import java.util.List;

public enum UnitName
{
    goldMiner, ironMiner, farmer, blacksmith, builder, carrier, guard, spy, infantry, bowman, cavalry;

    public static List<UnitName> getProductionUnits()
    {
        return List.of(goldMiner, ironMiner, farmer, blacksmith, builder);
    }

    public static UnitName getByProductionResourceType(ResourceName resourceType)
    {
        return switch (resourceType)
        {
            case gold -> goldMiner;
            case iron -> ironMiner;
            case food -> farmer;
            case tools -> blacksmith;
            case weapons -> blacksmith;
            case buildingPoints -> builder;
            default -> throw new IllegalArgumentException("Unknown unit for resource type: " + resourceType);
        };
    }

    public static List<UnitName> getMilitaryUnits()
    {
        return List.of(infantry, bowman, cavalry);
    }

    public static UnitName getByBuilding(BuildingName building)
    {
        return switch (building)
        {
            case market -> carrier;
            case spyGuild -> spy;
            case guardHouse -> guard;
            case barracks -> bowman; // TODO it shouldn't be only bowman
            case workshop -> blacksmith;
            case farm -> farmer;
            case goldMine -> goldMiner;
            case ironMine -> ironMiner;
            default -> null; // TODO
        };
    }

    public static UnitName from(String unit)
    {
        return switch (unit)
        {
            case "goldMiner" -> goldMiner;
            case "ironMiner" -> ironMiner;
            case "farmer" -> farmer;
            case "blacksmith" -> blacksmith;
            case "builder" -> builder;
            case "carrier" -> carrier;
            case "guard" -> guard;
            case "spy" -> spy;
            case "infantry" -> infantry;
            case "bowman" -> bowman;
            case "cavalry" -> cavalry;
            default -> throw new IllegalArgumentException("Unknown unit: " + unit);
        };
    }
}
