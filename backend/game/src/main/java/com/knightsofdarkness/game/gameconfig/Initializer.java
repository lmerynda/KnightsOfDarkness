package com.knightsofdarkness.game.gameconfig;

import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class Initializer {
    private Initializer()
    {
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
