package com.knightsofdarkness.common.kingdom;

import java.util.List;

public enum UnitName
{
    goldMiner, ironMiner, farmer, blacksmith, builder, carrier, guard, spy, infantry, bowmen, cavalry;

    public static List<UnitName> getProductionUnits()
    {
        return List.of(goldMiner, ironMiner, farmer, blacksmith, builder);
    }

    public static UnitName getByBuilding(BuildingName building)
    {
        return switch (building)
        {
            case market -> carrier;
            case spyGuild -> spy;
            case guardHouse -> guard;
            case barracks -> bowmen;
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
            case "goldMiners" -> goldMiner;
            case "ironMiners" -> ironMiner;
            case "farmers" -> farmer;
            case "blacksmiths" -> blacksmith;
            case "builders" -> builder;
            case "carriers" -> carrier;
            case "guards" -> guard;
            case "spies" -> spy;
            case "infantry" -> infantry;
            case "bowmen" -> bowmen;
            case "cavalry" -> cavalry;
            default -> throw new IllegalArgumentException("Unknown unit: " + unit);
        };
    }
}
