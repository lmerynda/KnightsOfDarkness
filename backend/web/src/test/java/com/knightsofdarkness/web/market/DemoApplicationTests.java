package com.knightsofdarkness.web.market;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DemoApplicationTests {
    @Autowired
    private MarketController controller;

    @Test
    @Disabled
    void contextLoads() throws Exception
    {
        assertThat(controller).isNotNull();
    }
}
