package com.knightsofdarkness.web.bots;

import static java.util.Collections.shuffle;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.knightsofdarkness.web.bots.model.BotEntity;

/**
 * Runs every bot once, each in its own transaction. Driven by BotClock in a live game and by the simulation edge in
 * simulations.
 */
@Component
public class Scheduler
{
    private static final Logger log = LoggerFactory.getLogger(Scheduler.class);
    private final IBotRepository botRepository;
    private final BotsRunner botsRunner;

    public Scheduler(IBotRepository botRepository, BotsRunner botsRunner)
    {
        this.botRepository = botRepository;
        this.botsRunner = botsRunner;
    }

    public void runBots()
    {
        List<BotEntity> bots = botRepository.findAllWithKingdoms();
        shuffle(bots);
        for (var bot : bots)
        {
            var kingdomName = bot.getKingdom().getName();
            botsRunner.runSingleBotActions(kingdomName);
            log.debug("[{}] actions done", kingdomName);
        }
    }
}
