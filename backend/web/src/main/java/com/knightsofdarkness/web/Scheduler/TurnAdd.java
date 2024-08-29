package com.knightsofdarkness.web.Scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.knightsofdarkness.web.Bots.BotsRunner;
import com.knightsofdarkness.web.Kingdom.KingdomService;

@Component
public class TurnAdd {
    private final Logger log = LoggerFactory.getLogger(BotsRunner.class);

    KingdomService kingdomService;

    public TurnAdd(KingdomService kingdomService)
    {
        this.kingdomService = kingdomService;
    }

    // TODO time value should be in game config
    @Scheduled(fixedRate = 2000000)
    public void addTurnEveryMinute()
    {
        log.info("Adding turn");
        kingdomService.addTurnForEveryone();
    }
}
