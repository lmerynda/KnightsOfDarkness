package com.merynda.kod.gameconfig;

import org.junit.jupiter.api.Test;

class InitializerTest {
    @Test
    void readGameConfig()
    {
        var config = Initializer.readGameConfig();
        System.out.println(config.toString());
    }
}