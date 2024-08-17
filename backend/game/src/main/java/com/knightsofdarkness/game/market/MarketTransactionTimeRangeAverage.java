package com.knightsofdarkness.game.market;

import java.time.Instant;
import java.util.UUID;

public class MarketTransactionTimeRangeAverage {
    UUID id;
    MarketResource resource;
    int averagePrice;
    int volume;
    Instant from;
    Instant to;

    public MarketTransactionTimeRangeAverage(UUID id, MarketResource resource, int averagePrice, int volume, Instant from, Instant to)
    {
        this.id = id;
        this.resource = resource;
        this.averagePrice = averagePrice;
        this.volume = volume;
        this.from = from;
        this.to = to;
    }

    public UUID getId()
    {
        return id;
    }

    public MarketResource getResource()
    {
        return resource;
    }

    public int getAveragePrice()
    {
        return averagePrice;
    }

    public int getVolume()
    {
        return volume;
    }

    public Instant getFrom()
    {
        return from;
    }

    public Instant getTo()
    {
        return to;
    }
}
