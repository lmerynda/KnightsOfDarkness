package com.knightsofdarkness.web.Market;

import java.util.UUID;

import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.game.market.MarketOffer;
import com.knightsofdarkness.game.market.MarketResource;

public class MarketOfferDto {
    public UUID id;
    public Kingdom kingdom;
    public MarketResource resource;
    public int count;
    public int price;

    public MarketOfferDto()
    {
    }

    public MarketOfferDto(Kingdom kingdom, MarketResource resource, int count, int price)
    {
        this.kingdom = kingdom;
        this.resource = resource;
        this.count = count;
        this.price = price;
    }

    public MarketOfferDto(UUID id, Kingdom kingdom, MarketResource resource, int count, int price)
    {
        this.id = id;
        this.kingdom = kingdom;
        this.resource = resource;
        this.count = count;
        this.price = price;
    }

    public MarketOffer toDomain()
    {
        return new MarketOffer(kingdom, resource, count, price);
    }

    public static MarketOfferDto fromDomain(MarketOffer offer)
    {
        return new MarketOfferDto(offer.getId(), offer.getKingdom(), offer.getResource(), offer.getCount(), offer.getPrice());
    }

    public String toString()
    {
        return "MarketOfferDto{" +
                "id=" + id +
                ", kingdom=" + kingdom +
                ", resource=" + resource +
                ", count=" + count +
                ", price=" + price +
                '}';
    }
}
