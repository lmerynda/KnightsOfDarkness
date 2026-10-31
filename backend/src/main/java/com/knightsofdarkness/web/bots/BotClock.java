package com.knightsofdarkness.web.bots;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * bots act on the wall clock in a live game; simulations turn the clock off and advance time themselves
 */
@Component
@ConditionalOnProperty(name = "game.clock.enabled", havingValue = "true", matchIfMissing = true)
public class BotClock
{
    private final Scheduler scheduler;

    public BotClock(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    @Scheduled(fixedRate = 1000 * 5)
    public void runBots()
    {
        scheduler.runBots();
    }
}
