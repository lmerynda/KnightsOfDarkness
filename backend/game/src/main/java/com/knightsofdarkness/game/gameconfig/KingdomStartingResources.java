package com.knightsofdarkness.game.gameconfig;

import java.util.EnumMap;
import java.util.Map;

import com.knightsofdarkness.common.kingdom.KingdomResourcesDto;
import com.knightsofdarkness.common.kingdom.ResourceName;

public record KingdomStartingResources(int land, int buildingPoints, int unemployed, int gold, int iron, int food, int tools, int weapons, int turns) {
    public int getCount(ResourceName name)
    {
        return switch (name)
        {
            case land -> land;
            case buildingPoints -> buildingPoints;
            case unemployed -> unemployed;
            case gold -> gold;
            case iron -> iron;
            case food -> food;
            case tools -> tools;
            case weapons -> weapons;
            case turns -> turns;
        };
    }

    public KingdomResourcesDto toDto()
    {
        return new KingdomResourcesDto(land, buildingPoints, unemployed, gold, iron, food, tools, weapons, turns);
    }

    public Map<ResourceName, Integer> toMap()
    {
        var map = new EnumMap<ResourceName, Integer>(ResourceName.class);
        map.put(ResourceName.land, land);
        map.put(ResourceName.buildingPoints, buildingPoints);
        map.put(ResourceName.unemployed, unemployed);
        map.put(ResourceName.gold, gold);
        map.put(ResourceName.iron, iron);
        map.put(ResourceName.food, food);
        map.put(ResourceName.tools, tools);
        map.put(ResourceName.weapons, weapons);
        map.put(ResourceName.turns, turns);
        return map;
    }
}
