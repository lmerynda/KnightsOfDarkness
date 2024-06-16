package com.knightsofdarkness.web.Kingdom;

import java.util.List;

import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.game.market.IMarket;
import com.knightsofdarkness.game.market.MarketOffer;

public class KingdomDto {
    public String name;
    public KingdomResourcesDto resources;
    public KingdomBuildingsDto buildings;
    public KingdomUnitsDto units;

    public KingdomDto()
    {
    }

    public KingdomDto(String name, KingdomResourcesDto resources, KingdomBuildingsDto buildings, KingdomUnitsDto units)
    {
        this.name = name;
        this.resources = resources;
        this.buildings = buildings;
        this.units = units;
    }

    public Kingdom toDomain(GameConfig config, IMarket market, List<MarketOffer> marketOffers)
    {
        return new Kingdom(name, config, market, resources.toDomain(), buildings.toDomain(), units.toDomain(), marketOffers);
    }

    public static KingdomDto fromDomain(Kingdom kingdom)
    {
        return new KingdomDto(kingdom.getName(), KingdomResourcesDto.fromDomain(kingdom.getResources()), KingdomBuildingsDto.fromDomain(kingdom.getBuildings()), KingdomUnitsDto.fromDomain(kingdom.getUnits()));
    }

    public String toString()
    {
        return "KingdomDto{" +
                "name='" + name +
                ", resources=" + resources +
                ", buildings=" + buildings +
                ", units=" + units +
                '}';
    }
}
