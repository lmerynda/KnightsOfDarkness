package com.knightsofdarkness.game;

import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.game.gameconfig.Initializer;

public class TestGameConfig {
    public GameConfig get()
    {
        return Initializer.readGameConfig();
    }
}
