package com.knightsofdarkness.storage.market;

import java.util.UUID;

import com.knightsofdarkness.common.market.MarketOfferDto;
import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.game.kingdom.Kingdom;
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
    @JoinColumn(name = "kingdom_name", nullable = false)
    private KingdomEntity kingdom;

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

    public MarketOffer toDomainModel(GameConfig gameConfig)
    {
        var kingdom = this.kingdom.toDomainModel(gameConfig);
        return new MarketOffer(id, kingdom, resource, count, price);
    }

    public MarketOfferDto toDto()
    {
        return new MarketOfferDto(id, kingdom.getName(), resource, count, price);
    }

    public MarketOffer toDomainModel(Kingdom kingdom)
    {
        return new MarketOffer(id, kingdom, resource, count, price);
    }

    public static MarketOfferEntity fromDomainModel(MarketOffer offer)
    {
        return new MarketOfferEntity(offer.getId(), KingdomEntity.fromDomainModel(offer.getSeller()), offer.getResource(), offer.getCount(), offer.getPrice());
    }

    public static MarketOfferEntity fromDomainModel(MarketOffer offer, KingdomEntity kingdomEntity)
    {
        return new MarketOfferEntity(offer.getId(), kingdomEntity, offer.getResource(), offer.getCount(), offer.getPrice());
    }

    public KingdomEntity getKingdom()
    {
        return kingdom;
    }
}
