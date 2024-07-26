package com.knightsofdarkness.web.Bots;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.knightsofdarkness.game.bot.BlacksmithBot;
import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.game.market.IMarket;
import com.knightsofdarkness.storage.kingdom.KingdomRepository;
import com.knightsofdarkness.web.Kingdom.KingdomService;
import com.knightsofdarkness.web.Market.MarketService;

@Component
public class BotsRunner {
    private final Logger log = LoggerFactory.getLogger(BotsRunner.class);

    private final KingdomService kingdomService;
    private final MarketService marketService;
    private final KingdomRepository kingdomRepository;
    private final GameConfig gameConfig;
    private final IMarket market;

    public BotsRunner(KingdomService kingdomService, MarketService marketService, KingdomRepository kingdomRepository, IMarket market, GameConfig gameConfig)
    {
        this.kingdomService = kingdomService;
        this.marketService = marketService;
        this.kingdomRepository = kingdomRepository;
        this.market = market;
        this.gameConfig = gameConfig;
    }

    // Runs every 10 seconds (units are in milliseconds)
    @Scheduled(fixedRate = 2000)
    public void runEvery10Seconds()
    {
        log.info("Running bots every 10 second");
        var blacksmithBotKingdom = kingdomRepository.getKingdomByName("BlacksmithBot");
        if (blacksmithBotKingdom.isEmpty())
        {
            log.error("BlacksmithBot kingdom not found");
            return;
        }

        var kingdom = blacksmithBotKingdom.get();

        var blacksmithBot = new BlacksmithBot(kingdom, market);
        runBotActions(blacksmithBot);
        log.info(blacksmithBot.getKingdomInfo());
        log.info("BlacksmithBot actions done");
    }

    @Transactional
    void runBotActions(BlacksmithBot blacksmithBot)
    {
        blacksmithBot.doAllActions();
        blacksmithBot.passTurn();
        kingdomRepository.update(blacksmithBot.getKingdom());
    }
}
