package com.knightsofdarkness.game.market;

// TODO think about better name
public class MarketOfferBuyResult {
    public MarketResource resource;
    public int count;
    public int pricePerUnit;
    public int totalCost;

    public MarketOfferBuyResult()
    {
    }

    public MarketOfferBuyResult(MarketResource resource, int count, int pricePerUnit, int totalCost)
    {
        this.resource = resource;
        this.count = count;
        this.pricePerUnit = pricePerUnit;
        this.totalCost = totalCost;
    }
}
