package com.knightsofdarkness.web.market.model;

import java.time.Instant;
import java.util.UUID;

import com.knightsofdarkness.common.market.MarketResource;
import com.knightsofdarkness.game.market.MarketTransactionTimeRangeAverage;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

@Entity
public class MarketTransactionTimeRangeAveragesEntity {
    @Id
    private UUID id;
    @Enumerated(EnumType.STRING)
    private MarketResource resource;
    private int averagePrice;
    private int volume;
    private Instant fromDate;
    private Instant toDate;

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

    public static MarketTransactionTimeRangeAveragesEntity fromDomainModel(MarketTransactionTimeRangeAverage averageSaleRecord)
    {
        return new MarketTransactionTimeRangeAveragesEntity(averageSaleRecord.getId(), averageSaleRecord.getResource(), averageSaleRecord.getAveragePrice(), averageSaleRecord.getVolume(), averageSaleRecord.getFrom(),
                averageSaleRecord.getTo());
    }

    public MarketTransactionTimeRangeAverage toDomainModel()
    {
        return new MarketTransactionTimeRangeAverage(id, resource, averagePrice, volume, fromDate, toDate);
    }
}
