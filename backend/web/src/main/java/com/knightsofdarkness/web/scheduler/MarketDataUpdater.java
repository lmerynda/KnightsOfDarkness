package com.knightsofdarkness.web.scheduler;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.knightsofdarkness.game.market.IMarket;

import jakarta.transaction.Transactional;

@Component
public class MarketDataUpdater {
    private final Logger log = LoggerFactory.getLogger(MarketDataUpdater.class);
    private final int fiveSeconds = 5000;
    private final int periodicity = fiveSeconds; // 60 * 60 * 1000; TODO fix after testing

    IMarket market;

    public MarketDataUpdater(IMarket market)
    {
        this.market = market;
    }

    @Scheduled(fixedRate = periodicity)
    @Transactional
    public void updateMarketData()
    {
        log.info("Updating Market transactions averages");
        var to = Instant.now();
        var from = to.minus(periodicity, ChronoUnit.MILLIS);
        market.updateMarketTransactionsAverages(from, to);
    }
}
