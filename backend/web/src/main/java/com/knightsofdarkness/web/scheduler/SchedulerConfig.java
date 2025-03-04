package com.knightsofdarkness.web.scheduler;

import java.time.Duration;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import com.knightsofdarkness.web.game.config.GameConfig;

@Configuration
@EnableScheduling
public class SchedulerConfig implements SchedulingConfigurer {
    private final GameConfig gameConfig;
    private final TurnAdd turnAdd;
    private final MarketDataUpdater marketDataUpdater;

    // Inject GameConfig and TurnAdd
    public SchedulerConfig(GameConfig gameConfig, TurnAdd turnAdd, MarketDataUpdater marketDataUpdater)
    {
        this.gameConfig = gameConfig;
        this.turnAdd = turnAdd;
        this.marketDataUpdater = marketDataUpdater;
    }

    @Override
    public void configureTasks(@NonNull ScheduledTaskRegistrar taskRegistrar)
    {
        Duration newTurnPeriodicity = Duration.ofMillis(gameConfig.common().newTurnPeriodicity());
        taskRegistrar.addFixedRateTask(turnAdd::addTurnPeriodically, newTurnPeriodicity);

        Duration marketAveragePeriodicity = Duration.ofMillis(gameConfig.market().marketAveragePeriodicity());
        taskRegistrar.addFixedRateTask(marketDataUpdater::updateMarketData, marketAveragePeriodicity);
    }
}
