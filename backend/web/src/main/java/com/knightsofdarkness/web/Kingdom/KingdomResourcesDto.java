package com.knightsofdarkness.web.Kingdom;

import com.knightsofdarkness.game.kingdom.KingdomResources;
import com.knightsofdarkness.game.kingdom.ResourceName;

public class KingdomResourcesDto {
    int land;
    int buildingPoints;
    int unemployed;
    int gold;
    int iron;
    int food;
    int tool;
    int weapons;
    int turn;

    public KingdomResourcesDto()
    {
    }

    public KingdomResourcesDto(int land, int buildingPoints, int unemployed, int gold, int iron, int food, int tool, int weapons, int turn)
    {
        this.land = land;
        this.buildingPoints = buildingPoints;
        this.unemployed = unemployed;
        this.gold = gold;
        this.iron = iron;
        this.food = food;
        this.tool = tool;
        this.weapons = weapons;
        this.turn = turn;
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
        kingdomResources.setCount(ResourceName.tools, tool);
        kingdomResources.setCount(ResourceName.weapons, weapons);
        kingdomResources.setCount(ResourceName.turns, turn);
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
            kingdomResources.getCount(ResourceName.turns)
        );
    }
}
