package com.knightsofdarkness.web.common.kingdom;

import java.util.List;

import com.knightsofdarkness.web.common.market.MarketResource;

public enum ResourceName
{
    land, buildingPoints, unemployed, gold, iron, food, tools, weapons, turns;

    public static List<ResourceName> productionResourceNames()
    {
        return List.of(gold, iron, food, tools, weapons, buildingPoints);
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
