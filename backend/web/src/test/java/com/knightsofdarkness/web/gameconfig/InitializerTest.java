package com.knightsofdarkness.web.gameconfig;

import org.junit.jupiter.api.Test;

import com.knightsofdarkness.web.game.config.Initializer;

class InitializerTest {
    @Test
    void readGameConfig()
    {
        var config = Initializer.readGameConfig();
        System.out.println(config.toString());
    }
}
