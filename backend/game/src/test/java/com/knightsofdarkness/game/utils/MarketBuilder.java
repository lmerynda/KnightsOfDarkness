package com.knightsofdarkness.game.utils;

import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.game.market.IMarket;
import com.knightsofdarkness.game.market.Market;
import com.knightsofdarkness.game.storage.IKingdomRepository;

public class MarketBuilder {
    MarketRepository repository = new MarketRepository();
    IKingdomRepository kingdomRepository = new KingdomRepository();
    GameConfig gameConfig;

    public MarketBuilder(GameConfig config)
    {
        gameConfig = config;
    }

    public MarketBuilder withRepository(MarketRepository repository)
    {
        this.repository = repository;
        return this;
    }

    public MarketBuilder withKingdomRepository(IKingdomRepository kingdomRepository)
    {
        this.kingdomRepository = kingdomRepository;
        return this;
    }

    public IMarket build()
    {
        return new Market(repository, kingdomRepository, gameConfig);
    }
}
