package com.knightsofdarkness.web.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.knightsofdarkness.web.kingdom.KingdomService;
import com.knightsofdarkness.web.round.RoundService;

@Component
public class TurnAdd
{
    private static final Logger log = LoggerFactory.getLogger(TurnAdd.class);
    private final KingdomService kingdomService;
    private final RoundService roundService;

    public TurnAdd(KingdomService kingdomService, RoundService roundService)
    {
        this.kingdomService = kingdomService;
        this.roundService = roundService;
    }

    public void addTurnPeriodically()
    {
        log.info("Adding turn");
        kingdomService.addTurnForEveryone();
        roundService.tick();
    }
}
