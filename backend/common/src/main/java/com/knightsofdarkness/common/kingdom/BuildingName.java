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
            case bowmen, infantry, cavalry -> barracks;
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
            case "houses" -> house;
            case "goldMines" -> goldMine;
            case "ironMines" -> ironMine;
            case "workshops" -> workshop;
            case "farms" -> farm;
            case "markets" -> market;
            case "barracks" -> barracks;
            case "guardHouses" -> guardHouse;
            case "spyGuilds" -> spyGuild;
            case "towers" -> tower;
            case "castles" -> castle;
            default -> throw new IllegalArgumentException("Unknown building: " + building);
        };
    }
}
