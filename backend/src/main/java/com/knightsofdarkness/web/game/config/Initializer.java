package com.knightsofdarkness.web.game.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

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

    /** the same game with turns coming every given period */
    public static GameConfig withTurnPeriod(GameConfig config, Duration period)
    {
        var mapper = new ObjectMapper();
        ObjectNode tree = mapper.valueToTree(config);
        ((ObjectNode) tree.get("common")).put("newTurnPeriodicity", Math.toIntExact(period.toMillis()));
        return mapper.convertValue(tree, GameConfig.class);
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
