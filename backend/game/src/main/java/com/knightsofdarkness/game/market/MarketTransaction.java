package com.knightsofdarkness.game.market;

import java.time.Instant;
import java.util.UUID;

import com.knightsofdarkness.common.market.MarketResource;

public class MarketTransaction {
    UUID id;
    MarketResource resource;
    String seller;
    String buyer;
    int price;
    int count;
    Instant date;

    public MarketTransaction()
    {
    }

    public MarketTransaction(UUID id, MarketResource resource, String seller, String buyer, int price, int count, Instant date)
    {
        this.id = id;
        this.resource = resource;
        this.seller = seller;
        this.buyer = buyer;
        this.price = price;
        this.count = count;
        this.date = date;
    }

    public UUID getId()
    {
        return id;
    }

    public MarketResource getResource()
    {
        return resource;
    }

    public String getSeller()
    {
        return seller;
    }

    public String getBuyer()
    {
        return buyer;
    }

    public int getPrice()
    {
        return price;
    }

    public int getCount()
    {
        return count;
    }

    public Instant getDate()
    {
        return date;
    }
}
