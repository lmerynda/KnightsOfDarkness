package com.knightsofdarkness.storage.market;

import java.util.UUID;

import com.knightsofdarkness.game.market.MarketOffer;
import com.knightsofdarkness.game.market.MarketResource;
import com.knightsofdarkness.storage.kingdom.KingdomEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class MarketOfferEntity {
    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "kingdom_id", nullable = false)
    private KingdomEntity kingdom;

    // TODO ask MM how to store enum refs in db
    @Enumerated(EnumType.STRING)
    private MarketResource resource;

    private int count;

    private int price;

    public MarketOfferEntity()
    {
    }

    public MarketOfferEntity(UUID id, KingdomEntity kingdom, MarketResource resource, int count, int price)
    {
        this.id = id;
        this.kingdom = kingdom;
        this.resource = resource;
        this.count = count;
        this.price = price;
    }

    public MarketOffer toDomainModel()
    {
        // TODO fix null kingdom
        return new MarketOffer(id, kingdom.toDomainModel(), resource, count, price);
    }

    public static MarketOfferEntity fromDomainModel(MarketOffer offer)
    {
        // TODO fix null kingdom
        return new MarketOfferEntity(offer.getId(), KingdomEntity.fromDomainModel(offer.getKingdom()), offer.getResource(), offer.getCount(), offer.getPrice());
    }
}
