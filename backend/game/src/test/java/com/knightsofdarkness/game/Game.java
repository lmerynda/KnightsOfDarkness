package com.knightsofdarkness.game;

import java.util.ArrayList;
import java.util.List;

import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.game.market.IMarket;

public class Game {
    GameConfig config;
    IMarket market;
    List<Kingdom> kingdoms = new ArrayList<>();

    public Game(GameConfig config, IMarket market)
    {
        this.config = config;
        this.market = market;
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
        this.kingdoms.add(kingdom);
    }
}
