package com.knightsofdarkness.game;

import com.knightsofdarkness.game.game.Game;
import com.knightsofdarkness.game.gameconfig.Initializer;
import com.knightsofdarkness.game.market.Market;

public class TestGame {
    public Game get()
    {
        var market = new Market();
        var config = Initializer.readGameConfig();

        return new Game(config, market);
    }
}
