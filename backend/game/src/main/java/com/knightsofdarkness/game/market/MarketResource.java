package com.knightsofdarkness.game.market;

public enum MarketResource
{
    iron, food, tools, weapons;

    public com.knightsofdarkness.common.market.MarketResource toMarketResource()
    {
        return com.knightsofdarkness.common.market.MarketResource.valueOf(this.name());
    }
}
