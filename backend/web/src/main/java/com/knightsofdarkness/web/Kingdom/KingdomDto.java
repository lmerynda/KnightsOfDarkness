package com.knightsofdarkness.web.Kingdom;

import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.game.market.IMarket;

public class KingdomDto {
    public String name;
    public KingdomResourcesDto resources;
    public KingdomBuildingsDto buildings;

    public KingdomDto()
    {
    }

    public KingdomDto(String name, KingdomResourcesDto resources, KingdomBuildingsDto buildings)
    {
        this.name = name;
        this.resources = resources;
        this.buildings = buildings;
    }

    public Kingdom toDomain(GameConfig config, IMarket market)
    {
        return new Kingdom(name, config, market, resources.toDomain(), buildings.toDomain(), null);
    }

    public static KingdomDto fromDomain(Kingdom kingdom)
    {
        return new KingdomDto(kingdom.getName(), KingdomResourcesDto.fromDomain(kingdom.getResources()), KingdomBuildingsDto.fromDomain(kingdom.getBuildings()));
    }

    public String toString()
    {
        return "KingdomDto{" +
                "name='" + name +
                ", resources=" + resources +
                ", buildings=" + buildings +
                '}';
    }
}
