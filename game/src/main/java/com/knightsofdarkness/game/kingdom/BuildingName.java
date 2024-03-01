package com.knightsofdarkness.game.kingdom;

public enum BuildingName
{
    house, goldMine, ironMine, workshop, farm, market, barracks, guardHouse, spyGuild, tower, castle;

    public static BuildingName getByUnit(UnitName unit)
    {
        return switch (unit)
        {
            case builder -> null;
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
}
