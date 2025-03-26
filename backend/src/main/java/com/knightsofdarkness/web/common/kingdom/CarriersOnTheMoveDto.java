package com.knightsofdarkness.web.common.kingdom;

import java.util.UUID;

import com.knightsofdarkness.web.common.market.MarketResource;

public class CarriersOnTheMoveDto {
    public UUID id;
    public String from;
    public String to;
    public int turnsLeft;
    public int carriersCount;
    public MarketResource resource;
    public int amount;

    public CarriersOnTheMoveDto(UUID id, String from, String to, int turnsLeft, int carriersCount, MarketResource resource, int amount)
    {
        this.id = id;
        this.from = from;
        this.to = to;
        this.turnsLeft = turnsLeft;
        this.carriersCount = carriersCount;
        this.resource = resource;
        this.amount = amount;
    }

    public String toString()
    {
        return "CarriersOnTheMoveDto{" +
                "id=" + id +
                ", from='" + from +
                ", to='" + to +
                ", turnsLeft=" + turnsLeft +
                ", carriersCount=" + carriersCount +
                ", resource=" + resource +
                ", amount=" + amount +
                '}';
    }
}
