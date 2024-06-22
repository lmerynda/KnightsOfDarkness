package com.knightsofdarkness.web.Kingdom;

import java.util.List;

import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.game.market.MarketOffer;
import com.knightsofdarkness.web.Market.MarketOfferDto;

public class KingdomDto {
    public String name;
    public KingdomResourcesDto resources;
    public KingdomBuildingsDto buildings;
    public KingdomUnitsDto units;
    public List<MarketOfferDto> marketOffers;

    public KingdomDto()
    {
    }

    public KingdomDto(String name, KingdomResourcesDto resources, KingdomBuildingsDto buildings, KingdomUnitsDto units, List<MarketOfferDto> marketOffers)
    {
        this.name = name;
        this.resources = resources;
        this.buildings = buildings;
        this.units = units;
        this.marketOffers = marketOffers;
    }

    public Kingdom toDomain(GameConfig config, List<MarketOffer> marketOffers)
    {
        return new Kingdom(name, config, resources.toDomain(), buildings.toDomain(), units.toDomain(), marketOffers);
    }

    public static KingdomDto fromDomain(Kingdom kingdom)
    {
        return new KingdomDto(kingdom.getName(), KingdomResourcesDto.fromDomain(kingdom.getResources()), KingdomBuildingsDto.fromDomain(kingdom.getBuildings()), KingdomUnitsDto.fromDomain(kingdom.getUnits()), kingdom
                .getMarketOffers().stream().map(MarketOfferDto::fromDomain).toList());
    }

    public String toString()
    {
        return "KingdomDto{" +
                "name='" + name +
                ", resources=" + resources +
                ", buildings=" + buildings +
                ", units=" + units +
                ", marketOffers=" + marketOffers +
                '}';
    }
}
