package com.uprzejmy.kod.gameconfig;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class Initializer
{
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

