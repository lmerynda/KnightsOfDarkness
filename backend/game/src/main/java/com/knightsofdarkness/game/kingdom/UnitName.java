package com.knightsofdarkness.game.kingdom;

import java.util.List;

public enum UnitName
{
    goldMiner, ironMiner, farmer, blacksmith, builder, carrier, guard, spy, infantry, bowmen, cavalry;

    static List<UnitName> getProductionUnits()
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
}
