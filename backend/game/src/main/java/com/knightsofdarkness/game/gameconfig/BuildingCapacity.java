package com.knightsofdarkness.game.gameconfig;

import com.knightsofdarkness.game.kingdom.BuildingName;

public record BuildingCapacity(int house, int goldMine, int ironMine, int workshop, int farm, int market, int barracks, int guardHouse, int spyGuild, int tower, int castle) {
    public int getCapacity(BuildingName name)
    {
        return switch (name)
        {
            case house -> house;
            case goldMine -> goldMine;
            case ironMine -> ironMine;
            case workshop -> workshop;
            case farm -> farm;
            case market -> market;
            case barracks -> barracks;
            case guardHouse -> guardHouse;
            case spyGuild -> spyGuild;
            case tower -> tower;
            case castle -> castle;
        };
    }
}
