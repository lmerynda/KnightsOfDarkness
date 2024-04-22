package com.knightsofdarkness.game.market;

import java.util.UUID;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.knightsofdarkness.game.Id;
import com.knightsofdarkness.game.kingdom.Kingdom;

@JsonSerialize
public class MarketOffer {
    UUID id;
    Kingdom kingdom;
    MarketResource resource;
    int count;
    int price;

    public MarketOffer()
    {
        this.id = Id.generate();
    }

    public MarketOffer(Kingdom kingdom, MarketResource resource, int count, int price)
    {
        this.id = Id.generate();
        this.kingdom = kingdom;
        this.resource = resource;
        this.count = count;
        this.price = price;
    }

    public Kingdom getKingdom()
    {
        return kingdom;
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

    public String toString()
    {
        return "MarketOffer{" +
                "id=" + id +
                ", kingdom=" + kingdom +
                ", resource=" + resource +
                ", count=" + count +
                ", price=" + price +
                '}';
    }
}
