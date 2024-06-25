package com.knightsofdarkness.common;

import java.util.UUID;

import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.game.market.MarketOffer;
import com.knightsofdarkness.game.market.MarketResource;

public class MarketOfferDto {
    public UUID id;
    public String sellerName;
    public MarketResource resource;
    public int count;
    public int price;

    public MarketOfferDto()
    {
    }

    public MarketOfferDto(String sellerName, MarketResource resource, int count, int price)
    {
        this.sellerName = sellerName;
        this.resource = resource;
        this.count = count;
        this.price = price;
    }

    public MarketOfferDto(UUID id, String sellerName, MarketResource resource, int count, int price)
    {
        this.id = id;
        this.sellerName = sellerName;
        this.resource = resource;
        this.count = count;
        this.price = price;
    }

    public MarketOffer toDomain(Kingdom seller)
    {
        return new MarketOffer(id, seller, resource, count, price);
    }

    public static MarketOfferDto fromDomain(MarketOffer offer)
    {
        return new MarketOfferDto(offer.getId(), offer.getSeller().getName(), offer.getResource(), offer.getCount(), offer.getPrice());
    }

    public String toString()
    {
        return "MarketOfferDto{" +
                "id=" + id +
                ", sellerName=" + sellerName +
                ", resource=" + resource +
                ", count=" + count +
                ", price=" + price +
                '}';
    }
}
