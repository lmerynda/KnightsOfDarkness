package com.knightsofdarkness.web.gameconfig;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import com.knightsofdarkness.web.game.config.Initializer;

class InitializerTest {
    @Test
    void theGame_shouldHandOutATurnEveryHourAndStoreTwoDaysOfThem()
    {
        var common = Initializer.readGameConfig().common();

        assertEquals(Duration.ofHours(1).toMillis(), common.newTurnPeriodicity());
        assertEquals(48, common.maxTurns());
    }

    @Test
    void aFasterClock_shouldChangeOnlyThePace()
    {
        var game = Initializer.readGameConfig();

        var local = Initializer.withTurnPeriod(game, Duration.ofSeconds(10));

        assertEquals(10000, local.common().newTurnPeriodicity());
        assertEquals(game.common().maxTurns(), local.common().maxTurns());
        assertEquals(game.attack(), local.attack());
        assertEquals(game.era(), local.era());
        assertEquals(game.events(), local.events());
    }
}
