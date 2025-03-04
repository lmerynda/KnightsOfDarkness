package com.knightsofdarkness.web;

import com.knightsofdarkness.web.game.config.GameConfig;

import net.bytebuddy.build.AccessControllerPlugin.Initializer;

public class TestGameConfig {
    public GameConfig get()
    {
        return Initializer.readGameConfig();
    }
}
