package com.knightsofdarkness.storage.market;

import com.knightsofdarkness.game.market.MarketOffer;
import com.knightsofdarkness.game.market.MarketResource;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "market_offers")
public class MarketOfferEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "resource")
    private MarketResource resource;

    @Column(name = "count")
    private int count;

    @Column(name = "price")
    private int price;

    public MarketOffer toDomainModel()
    {
        return new MarketOffer(null, resource, count, price);
    }
}
