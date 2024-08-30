package com.knightsofdarkness.web.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.knightsofdarkness.web.kingdom.KingdomService;

@Component
public class TurnAdd {
    private final Logger log = LoggerFactory.getLogger(TurnAdd.class);

    KingdomService kingdomService;

    public TurnAdd(KingdomService kingdomService)
    {
        this.kingdomService = kingdomService;
    }

    // TODO time value should be in game config
    @Scheduled(fixedRate = 1000 * 60)
    public void addTurnEveryMinute()
    {
        log.info("Adding turn");
        kingdomService.addTurnForEveryone();
    }
}
