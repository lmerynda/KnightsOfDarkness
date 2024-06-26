package com.knightsofdarkness.game;

import com.knightsofdarkness.game.gameconfig.Initializer;
import com.knightsofdarkness.game.utils.MarketBuilder;

public class TestGame {
    public Game get()
    {
        var market = new MarketBuilder().build();
        var config = Initializer.readGameConfig();

        return new Game(config, market);
    }
}
