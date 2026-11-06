package com.knightsofdarkness.web.market.model;

import java.time.Instant;
import java.util.UUID;

import com.knightsofdarkness.web.common.market.MarketResource;
import com.knightsofdarkness.web.common.market.MarketTradeDto;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

@Entity
public class MarketTransactionEntity {
    @Id
    UUID id;
    @Enumerated(EnumType.STRING)
    MarketResource resource;
    String seller;
    String buyer;
    int price;
    int count;
    Instant date;

    public MarketTransactionEntity()
    {
    }

    public MarketTransactionEntity(UUID id, MarketResource resource, String seller, String buyer, int price, int count, Instant date)
    {
        this.id = id;
        this.resource = resource;
        this.seller = seller;
        this.buyer = buyer;
        this.price = price;
        this.count = count;
        this.date = date;
    }

    public MarketTradeDto toTradeDto()
    {
        return new MarketTradeDto(resource, seller, buyer, price, count);
    }

    public MarketResource getResource()
    {
        return resource;
    }

    public Instant getDate()
    {
        return date;
    }
}
