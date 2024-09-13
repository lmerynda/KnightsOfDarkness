package com.knightsofdarkness.game.kingdom;

import java.util.Set;

import com.knightsofdarkness.common.market.MarketResource;

public enum ResourceName
{
    land, buildingPoints, unemployed, gold, iron, food, tools, weapons, turns;

    static Set<ResourceName> productionResourceNames()
    {
        return Set.of(gold, iron, food, tools, weapons, buildingPoints);
    }

    static ResourceName from(MarketResource resource)
    {
        return switch (resource)
        {
            case food -> food;
            case iron -> iron;
            case tools -> tools;
            case weapons -> weapons;
        };
    }
}
