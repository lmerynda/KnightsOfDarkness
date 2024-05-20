package com.knightsofdarkness.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.game.gameconfig.Initializer;

@Configuration
public class GameConfigService {
    // Bean annotation will make this resource managed by spring, the instance will be cached
    @Bean
    public GameConfig gameConfig()
    {
        return Initializer.readGameConfig();
    }
}
