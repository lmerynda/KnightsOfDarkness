package com.knightsofdarkness.game.kingdom;

import java.util.UUID;

import com.knightsofdarkness.common.market.MarketResource;

public class KingdomCarriersOnTheMove {
    UUID id;
    String targetKingdomName;
    int turnsLeft;
    int carriersCount;
    MarketResource resource;
    int resourceCount;

    public KingdomCarriersOnTheMove(UUID id, String targetKingdomName, int turnsLeft, int carriersCount, MarketResource resource, int resourceCount)
    {
        this.id = id;
        this.targetKingdomName = targetKingdomName;
        this.turnsLeft = turnsLeft;
        this.carriersCount = carriersCount;
        this.resource = resource;
        this.resourceCount = resourceCount;
    }

    public UUID getId()
    {
        return id;
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
