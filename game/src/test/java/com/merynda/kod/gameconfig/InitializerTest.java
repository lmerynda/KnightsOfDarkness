package com.merynda.kod.gameconfig;

import org.junit.jupiter.api.Test;

import com.merynda.kod.gameconfig.Initializer;

class InitializerTest {
    @Test
    void readGameConfig()
    {
        var config = Initializer.readGameConfig();
        System.out.println(config.toString());
    }
}