package com.knightsofdarkness.common.kingdom;

public enum BuildingName
{
    house, goldMine, ironMine, workshop, farm, market, barracks, guardHouse, spyGuild, tower, castle;

    public static BuildingName getByUnit(UnitName unit)
    {
        return switch (unit)
        {
            case builder -> null; // TODO pls fix this
            case carrier -> market;
            case spy -> spyGuild;
            case blacksmith -> workshop;
            case bowman, infantry, cavalry -> barracks;
            case farmer -> farm;
            case goldMiner -> goldMine;
            case guard -> guardHouse;
            case ironMiner -> ironMine;
        };
    }

    public static BuildingName from(String building)
    {
        return switch (building)
        {
            case "house" -> house;
            case "goldMine" -> goldMine;
            case "ironMine" -> ironMine;
            case "workshop" -> workshop;
            case "farm" -> farm;
            case "market" -> market;
            case "barracks" -> barracks;
            case "guardHouse" -> guardHouse;
            case "spyGuild" -> spyGuild;
            case "tower" -> tower;
            case "castle" -> castle;
            default -> throw new IllegalArgumentException("Unknown building: " + building);
        };
    }
}
