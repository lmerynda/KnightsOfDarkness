package com.merynda.kod;

import com.merynda.kod.game.Game;
import com.merynda.kod.gameconfig.Initializer;
import com.merynda.kod.market.Market;

public class TestGame {
    public Game get()
    {
        var market = new Market();
        var config = Initializer.readGameConfig();

        return new Game(config, market);
    }
}
