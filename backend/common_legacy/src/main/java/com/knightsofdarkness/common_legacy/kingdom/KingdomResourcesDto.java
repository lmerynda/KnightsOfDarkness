package com.knightsofdarkness.common_legacy.kingdom;

import com.knightsofdarkness.common.kingdom.ResourceName;
import com.knightsofdarkness.game.kingdom.KingdomResources;

public class KingdomResourcesDto {
    public int land;
    public int usedLand;
    public int buildingPoints;
    public int unemployed;
    public int gold;
    public int iron;
    public int food;
    public int tools;
    public int weapons;
    public int turns;

    public KingdomResourcesDto()
    {
    }

    @SuppressWarnings("java:S107")
    public KingdomResourcesDto(int land, int buildingPoints, int unemployed, int gold, int iron, int food, int tools, int weapons, int turns)
    {
        this.land = land;
        this.buildingPoints = buildingPoints;
        this.unemployed = unemployed;
        this.gold = gold;
        this.iron = iron;
        this.food = food;
        this.tools = tools;
        this.weapons = weapons;
        this.turns = turns;
    }

    public KingdomResources toDomain()
    {
        var kingdomResources = new KingdomResources();
        kingdomResources.setCount(ResourceName.land, land);
        kingdomResources.setCount(ResourceName.buildingPoints, buildingPoints);
        kingdomResources.setCount(ResourceName.unemployed, unemployed);
        kingdomResources.setCount(ResourceName.gold, gold);
        kingdomResources.setCount(ResourceName.iron, iron);
        kingdomResources.setCount(ResourceName.food, food);
        kingdomResources.setCount(ResourceName.tools, tools);
        kingdomResources.setCount(ResourceName.weapons, weapons);
        kingdomResources.setCount(ResourceName.turns, turns);
        return kingdomResources;
    }

    public static KingdomResourcesDto fromDomain(KingdomResources kingdomResources)
    {
        return new KingdomResourcesDto(
                kingdomResources.getCount(ResourceName.land),
                kingdomResources.getCount(ResourceName.buildingPoints),
                kingdomResources.getCount(ResourceName.unemployed),
                kingdomResources.getCount(ResourceName.gold),
                kingdomResources.getCount(ResourceName.iron),
                kingdomResources.getCount(ResourceName.food),
                kingdomResources.getCount(ResourceName.tools),
                kingdomResources.getCount(ResourceName.weapons),
                kingdomResources.getCount(ResourceName.turns));
    }
}
