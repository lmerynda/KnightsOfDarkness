package com.knightsofdarkness.game.utils;

import com.knightsofdarkness.game.market.IMarket;
import com.knightsofdarkness.game.market.Market;
import com.knightsofdarkness.game.storage.IKingdomRepository;

public class MarketBuilder {
    MarketRepository repository = new MarketRepository();
    IKingdomRepository kingdomRepository = new KingdomRepository();

    public IMarket build()
    {
        return new Market(repository, kingdomRepository);
    }
}
