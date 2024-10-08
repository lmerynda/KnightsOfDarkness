package com.knightsofdarkness.common.kingdom;

import java.util.Set;

import com.knightsofdarkness.common.market.MarketResource;

public enum ResourceName
{
    land, buildingPoints, unemployed, gold, iron, food, tools, weapons, turns;

    static Set<ResourceName> productionResourceNames()
    {
        return Set.of(gold, iron, food, tools, weapons, buildingPoints);
    }

    public static ResourceName from(String resource)
    {
        return switch (resource)
        {
            case "land" -> land;
            case "buildingPoints" -> buildingPoints;
            case "unemployed" -> unemployed;
            case "gold" -> gold;
            case "food" -> food;
            case "iron" -> iron;
            case "tools" -> tools;
            case "weapons" -> weapons;
            case "turns" -> turns;
            default -> throw new IllegalArgumentException("Unknown resource name: " + resource);
        };
    }

    public static ResourceName from(MarketResource resource)
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
