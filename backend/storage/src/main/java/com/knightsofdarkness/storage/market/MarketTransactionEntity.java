package com.knightsofdarkness.storage.market;

import java.time.Instant;
import java.util.UUID;

import com.knightsofdarkness.game.market.MarketResource;
import com.knightsofdarkness.game.market.MarketTransaction;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

@Entity
public class MarketTransactionEntity {
    @Id
    private UUID id;
    @Enumerated(EnumType.STRING)
    private MarketResource resource;
    private String seller;
    private String buyer;
    private int price;
    private int count;
    private Instant date;

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

    public static MarketTransactionEntity fromDomainModel(MarketTransaction transaction)
    {
        return new MarketTransactionEntity(transaction.getId(), transaction.getResource(), transaction.getSeller(), transaction.getBuyer(), transaction.getPrice(), transaction.getCount(), transaction.getDate());
    }
}
