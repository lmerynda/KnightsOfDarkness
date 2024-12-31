package com.knightsofdarkness.game;

import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.game.interactions.IKingdomInteractor;
import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.game.market.IMarket;
import com.knightsofdarkness.game.messaging.INotificationSystem;
import com.knightsofdarkness.game.storage.IKingdomRepository;

public class Game {
    GameConfig config;
    IMarket market;
    IKingdomRepository kingdomRepository;
    INotificationSystem notificationSystem;
    IKingdomInteractor kingdomInteractor;

    public Game(GameConfig config, IMarket market, IKingdomRepository kingdomRepository, INotificationSystem notificationSystem, IKingdomInteractor kingdomInteractor)
    {
        this.config = config;
        this.market = market;
        this.kingdomRepository = kingdomRepository;
        this.notificationSystem = notificationSystem;
        this.kingdomInteractor = kingdomInteractor;
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

    public IKingdomInteractor getKingdomInteractor()
    {
        return kingdomInteractor;
    }

    public IKingdomRepository getKingdomRepository()
    {
        return kingdomRepository;
    }

    public void addKingdom(Kingdom kingdom)
    {
        kingdomRepository.add(kingdom);
    }
}
