package com.knightsofdarkness.game.market;

import java.util.UUID;

import com.knightsofdarkness.common.market.MarketOfferDto;
import com.knightsofdarkness.common.market.MarketResource;
import com.knightsofdarkness.game.kingdom.Kingdom;

public class MarketOffer {
    UUID id;
    Kingdom seller;
    MarketResource resource;
    int count;
    int price;

    public MarketOffer(UUID id, Kingdom seller, MarketResource resource, int count, int price)
    {
        this.id = id;
        this.seller = seller;
        this.resource = resource;
        this.count = count;
        this.price = price;
    }

    public Kingdom getSeller()
    {
        return seller;
    }

    public MarketResource getResource()
    {
        return resource;
    }

    public int getCount()
    {
        return count;
    }

    public int getPrice()
    {
        return price;
    }

    public UUID getId()
    {
        return id;
    }

    public MarketOfferDto toDto()
    {
        return new MarketOfferDto(id, seller.getName(), resource, count, price);
    }

    public String toString()
    {
        return "MarketOffer{" +
                "id=" + id +
                ", seller=" + seller.getName() +
                ", resource=" + resource +
                ", count=" + count +
                ", price=" + price +
                '}';
    }
}
