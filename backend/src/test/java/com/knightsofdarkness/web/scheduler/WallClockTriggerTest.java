package com.knightsofdarkness.web.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.support.SimpleTriggerContext;

class WallClockTriggerTest {
    private final WallClockTrigger hourly = new WallClockTrigger(Duration.ofHours(1));

    private static Instant at(String time)
    {
        return Instant.parse("2026-11-03T" + time + "Z");
    }

    private Instant next(Instant now, Instant lastScheduled)
    {
        var context = new SimpleTriggerContext(Clock.fixed(now, ZoneOffset.UTC));
        context.update(lastScheduled, lastScheduled, lastScheduled);
        return hourly.nextExecution(context);
    }

    @Test
    void aServerStartedMidHour_shouldHandOutTheNextTurnOnTheHour()
    {
        assertEquals(at("14:00:00"), hourly.nextExecution(new SimpleTriggerContext(Clock.fixed(at("13:20:15"), ZoneOffset.UTC))));
        // a restart a moment after a turn came gives no second one
        assertEquals(at("15:00:00"), hourly.nextExecution(new SimpleTriggerContext(Clock.fixed(at("14:00:00.300"), ZoneOffset.UTC))));
    }

    @Test
    void aTurnThatFiredEarly_shouldNotComeTwiceInOneHour()
    {
        assertEquals(at("15:00:00"), next(at("13:59:59.990"), at("14:00:00")));
        assertEquals(at("15:00:00"), next(at("14:00:00.020"), at("14:00:00")));
    }

    @Test
    void shortPeriods_shouldAlignToTheClockToo()
    {
        assertEquals(at("13:20:20"), WallClockTrigger.nextAfter(at("13:20:15"), Duration.ofSeconds(10)));
        assertEquals(at("13:20:30"), WallClockTrigger.nextAfter(at("13:20:20"), Duration.ofSeconds(10)));
    }
}
