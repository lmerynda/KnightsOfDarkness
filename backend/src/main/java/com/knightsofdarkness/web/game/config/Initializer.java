package com.knightsofdarkness.web.game.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class Initializer {
    private Initializer()
    {
    }

    public static GameConfig readGameConfig(Path path)
    {
        try (var input = Files.newInputStream(path))
        {
            return new ObjectMapper().readValue(input, GameConfig.class);
        } catch (IOException e)
        {
            throw new IllegalStateException("Cannot read game config " + path, e);
        }
    }

    public static GameConfig readGameConfig()
    {
        var mapper = new ObjectMapper();

        try (var input = Thread.currentThread().getContextClassLoader().getResourceAsStream("gameconfig.json"))
        {
            return mapper.readValue(input, GameConfig.class);
        } catch (IOException | NullPointerException e)
        {
            throw new RuntimeException(e);
        }
    }
}
