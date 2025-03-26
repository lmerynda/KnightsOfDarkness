package com.knightsofdarkness.web.kingdom.model;

import java.util.UUID;

import com.knightsofdarkness.web.common.kingdom.CarriersOnTheMoveDto;
import com.knightsofdarkness.web.common.market.MarketResource;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class KingdomCarriersOnTheMoveEntity {
    @Id
    UUID id;

    @ManyToOne
    @JoinColumn(name = "kingdom_name", nullable = false)
    KingdomEntity kingdom;

    String targetKingdomName;

    int turnsLeft;

    int carriersCount;

    @Enumerated(EnumType.STRING)
    MarketResource resource;

    int resourceCount;

    public KingdomCarriersOnTheMoveEntity()
    {
    }

    public KingdomCarriersOnTheMoveEntity(UUID id, KingdomEntity kingdom, String targetKingdomName, int turnsLeft, int carriersCount, MarketResource resource, int resourceCount)
    {
        this.id = id;
        this.kingdom = kingdom;
        this.targetKingdomName = targetKingdomName;
        this.turnsLeft = turnsLeft;
        this.carriersCount = carriersCount;
        this.resource = resource;
        this.resourceCount = resourceCount;
    }

    public CarriersOnTheMoveDto toDto()
    {
        return new CarriersOnTheMoveDto(id, kingdom.getName(), targetKingdomName, turnsLeft, carriersCount, resource, resourceCount);
    }

    public UUID getId()
    {
        return id;
    }

    public KingdomEntity getKingdom()
    {
        return kingdom;
    }

    public String getTargetKingdomName()
    {
        return targetKingdomName;
    }

    public int getTurnsLeft()
    {
        return turnsLeft;
    }

    public int getCarriersCount()
    {
        return carriersCount;
    }

    public MarketResource getResource()
    {
        return resource;
    }

    public int getResourceCount()
    {
        return resourceCount;
    }
}
