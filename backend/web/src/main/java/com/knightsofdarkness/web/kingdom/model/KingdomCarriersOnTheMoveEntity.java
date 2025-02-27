package com.knightsofdarkness.web.kingdom.model;

import java.util.UUID;

import com.knightsofdarkness.common.kingdom.CarriersOnTheMoveDto;
import com.knightsofdarkness.common.market.MarketResource;
import com.knightsofdarkness.game.kingdom.KingdomCarriersOnTheMove;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class KingdomCarriersOnTheMoveEntity {
    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "kingdom_name", nullable = false)
    private KingdomEntity kingdom;

    private String targetKingdomName;

    private int turnsLeft;

    private int carriersCount;

    @Enumerated(EnumType.STRING)
    private MarketResource resource;

    private int resourceCount;

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

    public KingdomCarriersOnTheMove toDomainModel()
    {
        return new KingdomCarriersOnTheMove(id, targetKingdomName, turnsLeft, carriersCount, resource, resourceCount);
    }

    public static KingdomCarriersOnTheMoveEntity fromDomainModel(KingdomCarriersOnTheMove carriersOnTheMove, KingdomEntity kingdom)
    {
        return new KingdomCarriersOnTheMoveEntity(carriersOnTheMove.getId(), kingdom, carriersOnTheMove.getTargetKingdomName(), carriersOnTheMove.getTurnsLeft(), carriersOnTheMove.getCarriersCount(),
                carriersOnTheMove.getResource(), carriersOnTheMove.getResourceCount());
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
