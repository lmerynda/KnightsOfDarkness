package com.knightsofdarkness.storage.kingdom;

import com.knightsofdarkness.common.KingdomResourcesDto;
import com.knightsofdarkness.game.kingdom.KingdomResources;
import com.knightsofdarkness.game.kingdom.ResourceName;

import jakarta.persistence.Embeddable;

@Embeddable
class KingdomResourcesEntity {
    int land;
    int buildingPoints;
    int unemployed;
    int gold;
    int iron;
    int food;
    int tool;
    int weapons;
    int turn;

    public KingdomResourcesEntity()
    {
    }

    @SuppressWarnings("java:S107")
    public KingdomResourcesEntity(int land, int buildingPoints, int unemployed, int gold, int iron, int food, int tool, int weapons, int turn)
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

    public KingdomResources toDomainModel()
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

    public KingdomResourcesDto toDto()
    {
        return new KingdomResourcesDto(land, buildingPoints, unemployed, gold, iron, food, tool, weapons, turn);
    }

    public static KingdomResourcesEntity fromDomainModel(KingdomResources kingdomResources)
    {
        return new KingdomResourcesEntity(
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