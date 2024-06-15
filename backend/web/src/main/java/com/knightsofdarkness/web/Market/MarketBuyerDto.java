package com.knightsofdarkness.web.Market;

public class MarketBuyerDto {
    public String buyer;
    public int count;

    public MarketBuyerDto()
    {
    }

    public MarketBuyerDto(String buyer, int count)
    {
        this.buyer = buyer;
        this.count = count;
    }

    public String toString()
    {
        return "MarketBuyerDto{" +
                "buyer=" + buyer +
                ", count=" + count +
                '}';
    }
}
