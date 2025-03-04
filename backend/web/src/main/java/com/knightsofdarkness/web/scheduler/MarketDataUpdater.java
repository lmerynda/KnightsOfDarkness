package com.knightsofdarkness.web.scheduler;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.market.IMarket;

import jakarta.transaction.Transactional;

@Component
public class MarketDataUpdater {
    private final Logger log = LoggerFactory.getLogger(MarketDataUpdater.class);
    private final IMarket market;
    private final GameConfig gameConfig;

    public MarketDataUpdater(IMarket market, GameConfig gameConfig)
    {
        this.market = market;
        this.gameConfig = gameConfig;
    }

    @Transactional
    public void updateMarketData()
    {
        log.info("Updating Market transactions averages");

        long periodicity = gameConfig.market().marketAveragePeriodicity();

        var to = Instant.now();
        var from = to.minus(periodicity, ChronoUnit.MILLIS);

        market.updateMarketTransactionsAverages(from, to);
    }
}
