package com.knightsofdarkness.web.scheduler;

import java.time.Duration;
import java.time.Instant;

import org.springframework.lang.NonNull;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;

/**
 * fires on the wall clock's whole periods, on the hour for hourly turns, so a restart neither hands out an extra turn nor
 * delays the next one, and every ruler can tell when it comes
 */
public record WallClockTrigger(Duration period) implements Trigger {
    @Override
    public Instant nextExecution(@NonNull TriggerContext context)
    {
        var now = context.getClock().instant();
        var last = context.lastScheduledExecution();
        return nextAfter(last != null && last.isAfter(now) ? last : now, period);
    }

    /** the first whole period strictly after the given moment */
    public static Instant nextAfter(Instant moment, Duration period)
    {
        long millis = period.toMillis();
        return Instant.ofEpochMilli(Math.floorDiv(moment.toEpochMilli(), millis) * millis + millis);
    }
}
