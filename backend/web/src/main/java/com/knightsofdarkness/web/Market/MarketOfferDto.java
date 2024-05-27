package com.knightsofdarkness.web.Market;

import java.util.UUID;

import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.game.market.MarketOffer;
import com.knightsofdarkness.game.market.MarketResource;

public class MarketOfferDto {
    public UUID id;
    public String kingdomName;
    public MarketResource resource;
    public int count;
    public int price;

    public MarketOfferDto()
    {
    }

    public MarketOfferDto(String kingdomName, MarketResource resource, int count, int price)
    {
        this.kingdomName = kingdomName;
        this.resource = resource;
        this.count = count;
        this.price = price;
    }

    public MarketOfferDto(UUID id, String kingdomName, MarketResource resource, int count, int price)
    {
        this.id = id;
        this.kingdomName = kingdomName;
        this.resource = resource;
        this.count = count;
        this.price = price;
    }

    public MarketOffer toDomain(Kingdom kingdom)
    {
        return new MarketOffer(id, kingdom, resource, count, price);
    }

    public static MarketOfferDto fromDomain(MarketOffer offer)
    {
        return new MarketOfferDto(offer.getId(), offer.getKingdom().getName(), offer.getResource(), offer.getCount(), offer.getPrice());
    }

    public String toString()
    {
        return "MarketOfferDto{" +
                "id=" + id +
                ", kingdomName=" + kingdomName +
                ", resource=" + resource +
                ", count=" + count +
                ", price=" + price +
                '}';
    }
}
