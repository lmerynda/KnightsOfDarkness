package com.knightsofdarkness.game.gameconfig;

import com.knightsofdarkness.common.kingdom.BuildingName;

public record BuildingPointCosts(int houses, int goldMines, int ironMines, int workshops, int farms, int markets, int barracks, int guardHouses, int spyGuilds, int towers, int castles) {
    public int getCost(BuildingName name)
    {
        return switch (name)
        {
            case house -> houses;
            case goldMine -> goldMines;
            case ironMine -> ironMines;
            case workshop -> workshops;
            case farm -> farms;
            case market -> markets;
            case barracks -> barracks;
            case guardHouse -> guardHouses;
            case spyGuild -> spyGuilds;
            case tower -> towers;
            case castle -> castles;
        };
    }
}
