package com.knightsofdarkness.web.bots;

import static java.util.Collections.shuffle;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.knightsofdarkness.web.bots.model.BotEntity;

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

    // Runs every 10 seconds (units are in milliseconds)
    // @Scheduled(fixedRate = 1000 * 60 * 60)
    @Scheduled(fixedRate = 1000 * 5)
    public void runEvery10Seconds()
    {
        log.info("Running bots every 10 second");
        List<BotEntity> bots = botRepository.findAllWithKingdoms();
        shuffle(bots);
        for (var bot : bots)
        {
            var kingdomName = bot.getKingdom().getName();
            botsRunner.runSingleBotActions(kingdomName);
            log.info("[{}] actions done", kingdomName);
        }
    }
}
