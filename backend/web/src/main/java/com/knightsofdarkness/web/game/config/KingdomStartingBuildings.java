package com.knightsofdarkness.web.game.config;

import java.util.Map;

import com.knightsofdarkness.common.kingdom.BuildingName;
import com.knightsofdarkness.common.kingdom.KingdomBuildingsDto;

public record KingdomStartingBuildings(int house, int goldMine, int ironMine, int workshop, int farm, int market, int barracks, int guardHouse, int spyGuild, int tower, int castle) {
    public int getCount(BuildingName name)
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

    public KingdomBuildingsDto toDto()
    {
        return new KingdomBuildingsEntityDto(house, goldMine, ironMine, workshop, farm, market, barracks, guardHouse, spyGuild, tower, castle);
    }

    public Map<BuildingName, Integer> toMap()
    {
        return toDto().getBuildings();
    }
}
