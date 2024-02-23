package com.merynda.kod;

import com.merynda.kod.gameconfig.GameConfig;
import com.merynda.kod.gameconfig.Initializer;

public class TestGameConfig {
    public GameConfig get()
    {
        return Initializer.readGameConfig();
    }
}
