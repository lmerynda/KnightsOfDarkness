package com.knightsofdarkness.web.market.model;

import java.time.Instant;
import java.util.UUID;

import com.knightsofdarkness.common.market.MarketResource;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

@Entity
public class MarketTransactionTimeRangeAveragesEntity {
    @Id
    UUID id;
    @Enumerated(EnumType.STRING)
    MarketResource resource;
    int averagePrice;
    int volume;
    Instant fromDate;
    Instant toDate;

    public MarketTransactionTimeRangeAveragesEntity()
    {
    }

    public MarketTransactionTimeRangeAveragesEntity(UUID id, MarketResource resource, int averagePrice, int volume, Instant fromDate, Instant toDate)
    {
        this.id = id;
        this.resource = resource;
        this.averagePrice = averagePrice;
        this.volume = volume;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public MarketResource getResource()
    {
        return resource;
    }
}
