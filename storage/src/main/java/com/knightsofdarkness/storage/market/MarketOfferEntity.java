package com.knightsofdarkness.storage.market;

import com.knightsofdarkness.game.market.MarketOffer;
import com.knightsofdarkness.game.market.MarketResource;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class MarketOfferEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO ask MM how to store enum refs in db
    @Enumerated(EnumType.STRING)
    private MarketResource resource;

    private int count;

    private int price;

    public MarketOffer toDomainModel()
    {
        return new MarketOffer(null, resource, count, price);
    }
}
