package com.knightsofdarkness.web.market.model;

import java.util.UUID;

import com.knightsofdarkness.common.market.MarketOfferDto;
import com.knightsofdarkness.common.market.MarketResource;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;

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
    // TODO rename to seller?
    private KingdomEntity kingdom;

    @Enumerated(EnumType.STRING)
    private MarketResource resource;

    public int count;

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

    public MarketOfferDto toDto()
    {
        return new MarketOfferDto(id, kingdom.getName(), resource, count, price);
    }

    // TODO check if really needed
    public UUID getId()
    {
        return id;
    }

    public KingdomEntity getSeller()
    {
        return kingdom;
    }

    public MarketResource getResource()
    {
        return resource;
    }

    public int getCount()
    {
        return count;
    }

    public int getPrice()
    {
        return price;
    }
}
