package com.knightsofdarkness.game.gameconfig;

import org.junit.jupiter.api.Test;

import net.bytebuddy.build.AccessControllerPlugin.Initializer;

class InitializerTest {
    @Test
    void readGameConfig()
    {
        var config = Initializer.readGameConfig();
        System.out.println(config.toString());
    }
}
