package com.knightsofdarkness.web.utils;

import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.kingdom.IKingdomRepository;
import com.knightsofdarkness.web.market.IMarket;
import com.knightsofdarkness.web.market.model.Market;
import com.knightsofdarkness.web.messaging.INotificationSystem;

public class MarketBuilder {
    MarketRepository repository = new MarketRepository();
    IKingdomRepository kingdomRepository = new KingdomRepository();
    INotificationSystem notificationSystem;
    GameConfig gameConfig;

    public MarketBuilder(GameConfig config, INotificationSystem notificationSystem)
    {
        gameConfig = config;
        this.notificationSystem = notificationSystem;
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
        return new Market(repository, kingdomRepository, notificationSystem, gameConfig);
    }
}
