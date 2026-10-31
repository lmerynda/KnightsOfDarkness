package com.knightsofdarkness.web;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.game.config.Initializer;

@Configuration
public class GameConfigService {
    /**
     * the bundled gameconfig.json, or the file named by game.config-path so simulations can try balance variants
     */
    @Bean
    public GameConfig gameConfig(@Value("${game.config-path:}") String configPath)
    {
        return configPath.isBlank() ? Initializer.readGameConfig() : Initializer.readGameConfig(Path.of(configPath));
    }
}
