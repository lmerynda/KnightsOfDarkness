package com.uprzejmy.kod;

import com.uprzejmy.kod.gameconfig.GameConfig;
import com.uprzejmy.kod.gameconfig.Initializer;

public class TestGameConfig
{
    public GameConfig get()
    {
        return Initializer.readGameConfig();
    }
}
