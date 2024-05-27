package com.knightsofdarkness.storage.market;

import java.util.UUID;

import com.knightsofdarkness.game.market.MarketOffer;
import com.knightsofdarkness.game.market.MarketResource;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

@Entity
public class MarketOfferEntity {
    @Id
    private UUID id;

    // TODO ask MM how to store enum refs in db
    @Enumerated(EnumType.STRING)
    private MarketResource resource;

    private int count;

    private int price;

    public MarketOfferEntity()
    {
    }

    public MarketOfferEntity(UUID id, MarketResource resource, int count, int price)
    {
        this.id = id;
        this.resource = resource;
        this.count = count;
        this.price = price;
    }

    public MarketOffer toDomainModel()
    {
        // TODO fix null kingdom
        return new MarketOffer(id, null, resource, count, price);
    }

    public static MarketOfferEntity fromDomainModel(MarketOffer offer)
    {
        return new MarketOfferEntity(offer.getId(), offer.getResource(), offer.getCount(), offer.getPrice());
    }
}
