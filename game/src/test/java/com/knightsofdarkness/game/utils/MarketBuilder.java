package com.knightsofdarkness.game.utils;

import com.knightsofdarkness.game.market.Market;

public class MarketBuilder {
    MarketRepository repository = new MarketRepository();

    public Market build()
    {
        return new Market(repository);
    }
}
