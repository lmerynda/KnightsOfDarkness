package com.knightsofdarkness.game.game;

import java.util.ArrayList;
import java.util.List;

import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.game.market.Market;

public class Game {
    GameConfig config;
    Market market;
    List<Kingdom> kingdoms = new ArrayList<>();

    public Game(GameConfig config, Market market)
    {
        this.config = config;
        this.market = market;
    }

    public GameConfig getConfig()
    {
        return config;
    }

    public Market getMarket()
    {
        return market;
    }

    public void addKingdom(Kingdom kingdom)
    {
        this.kingdoms.add(kingdom);
    }
}
