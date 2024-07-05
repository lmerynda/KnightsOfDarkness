package com.knightsofdarkness.common;

public class MarketBuyerDto {
    public int count;

    public MarketBuyerDto()
    {
    }

    public MarketBuyerDto(int count)
    {
        this.count = count;
    }

    public String toString()
    {
        return "MarketBuyerDto{" +
                ", count=" + count +
                '}';
    }
}
