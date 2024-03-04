package com.knightsofdarkness.game.market;

import com.knightsofdarkness.game.kingdom.Kingdom;

public class MarketOffer {
    long id;
    Kingdom kingdom;
    MarketResource resource;
    int count;
    int price;

    public MarketOffer(Kingdom kingdom, MarketResource resource, int count, int price)
    {
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

    public long getId()
    {
        return id;
    }
}
