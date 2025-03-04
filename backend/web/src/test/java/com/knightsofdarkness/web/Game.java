package com.knightsofdarkness.web;

import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.kingdom.IKingdomInteractor;
import com.knightsofdarkness.web.kingdom.IKingdomRepository;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;
import com.knightsofdarkness.web.market.IMarket;
import com.knightsofdarkness.web.messaging.legacy.INotificationSystem;

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

    public void addKingdom(KingdomEntity kingdom)
    {
        kingdomRepository.add(kingdom);
    }
}
