package com.knightsofdarkness.web.legacy;

import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.game.config.Initializer;

public class TestGameConfig {
    public GameConfig get()
    {
        return Initializer.readGameConfig();
    }
}
