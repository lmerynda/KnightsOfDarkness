package com.knightsofdarkness.web.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.knightsofdarkness.web.kingdom.KingdomService;

@Component
public class TurnAdd {
    private final Logger log = LoggerFactory.getLogger(TurnAdd.class);
    private final KingdomService kingdomService;

    public TurnAdd(KingdomService kingdomService)
    {
        this.kingdomService = kingdomService;
    }

    public void addTurnPeriodically()
    {
        log.info("Adding turn");
        kingdomService.addTurnForEveryone();
    }
}