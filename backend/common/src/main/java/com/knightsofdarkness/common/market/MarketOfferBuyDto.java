package com.knightsofdarkness.common.market;

public class MarketOfferBuyDto {
    public int count;

    public MarketOfferBuyDto()
    {
    }

    public MarketOfferBuyDto(int count)
    {
        this.count = count;
    }

    public String toString()
    {
        return "MarketOfferBuyDto{" +
                ", count=" + count +
                '}';
    }
}
