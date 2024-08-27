package com.knightsofdarkness.game;

import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.game.market.IMarket;
import com.knightsofdarkness.game.storage.IKingdomRepository;

public class Game {
    GameConfig config;
    IMarket market;
    IKingdomRepository kingdomRepository;

    public Game(GameConfig config, IMarket market, IKingdomRepository kingdomRepository)
    {
        this.config = config;
        this.market = market;
        this.kingdomRepository = kingdomRepository;
    }

    public GameConfig getConfig()
    {
        return config;
    }

    public IMarket getMarket()
    {
        return market;
    }

    public void addKingdom(Kingdom kingdom)
    {
        kingdomRepository.add(kingdom);
    }
}
