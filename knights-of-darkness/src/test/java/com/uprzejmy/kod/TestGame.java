package com.uprzejmy.kod;

import com.uprzejmy.kod.game.Game;
import com.uprzejmy.kod.gameconfig.Initializer;
import com.uprzejmy.kod.market.Market;

public class TestGame
{
    public Game get()
    {
        var market = new Market();
        var config = Initializer.readGameConfig();

        return new Game(config, market);
    }
}
