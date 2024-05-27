package com.knightsofdarkness.game.utils;

import com.knightsofdarkness.game.market.IMarket;
import com.knightsofdarkness.game.market.Market;

public class MarketBuilder {
    MarketRepository repository = new MarketRepository();

    public IMarket build()
    {
        return new Market(repository);
    }
}
