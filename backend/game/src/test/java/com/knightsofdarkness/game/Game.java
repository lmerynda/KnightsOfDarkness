package com.knightsofdarkness.game;

import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.game.market.IMarket;
import com.knightsofdarkness.game.messaging.INotificationSystem;
import com.knightsofdarkness.game.storage.IKingdomRepository;

public class Game {
    GameConfig config;
    IMarket market;
    IKingdomRepository kingdomRepository;
    INotificationSystem notificationSystem;

    public Game(GameConfig config, IMarket market, IKingdomRepository kingdomRepository, INotificationSystem notificationSystem)
    {
        this.config = config;
        this.market = market;
        this.kingdomRepository = kingdomRepository;
        this.notificationSystem = notificationSystem;
    }

    public GameConfig getConfig()
    {
        return config;
    }

    public IMarket getMarket()
    {
        return market;
    }

    public INotificationSystem getNotificationSystem()
    {
        return notificationSystem;
    }

    public void addKingdom(Kingdom kingdom)
    {
        kingdomRepository.add(kingdom);
    }
}
