package com.knightsofdarkness.game.gameconfig;

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
}
