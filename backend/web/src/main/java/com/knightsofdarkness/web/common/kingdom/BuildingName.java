package com.knightsofdarkness.web.common.kingdom;

import java.util.Optional;

public enum BuildingName
{
    house, goldMine, ironMine, workshop, farm, market, barracks, guardHouse, spyGuild, tower, castle;

    public static Optional<BuildingName> getByUnit(UnitName unit)
    {
        return switch (unit)
        {
            case builder -> Optional.empty();
            case carrier -> Optional.of(market);
            case spy -> Optional.of(spyGuild);
            case blacksmith -> Optional.of(workshop);
            case bowman, infantry, cavalry -> Optional.of(barracks);
            case farmer -> Optional.of(farm);
            case goldMiner -> Optional.of(goldMine);
            case guard -> Optional.of(guardHouse);
            case ironMiner -> Optional.of(ironMine);
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
