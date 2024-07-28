package com.knightsofdarkness.web.Bots;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.knightsofdarkness.game.bot.BlacksmithBot;
import com.knightsofdarkness.game.bot.Bot;
import com.knightsofdarkness.game.bot.FarmerBot;
import com.knightsofdarkness.game.bot.GoldMinerBot;
import com.knightsofdarkness.game.bot.IronMinerBot;
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
    private final List<String> botNames = Arrays.asList("BlacksmithBot", "FarmerBot", "IronMinerBot", "GoldMinerBot");

    public BotsRunner(KingdomService kingdomService, MarketService marketService, KingdomRepository kingdomRepository, IMarket market, GameConfig gameConfig)
    {
        this.kingdomService = kingdomService;
        this.marketService = marketService;
        this.kingdomRepository = kingdomRepository;
        this.market = market;
        this.gameConfig = gameConfig;
    }

    // Runs every 10 seconds (units are in milliseconds)
    @Scheduled(fixedRate = 500)
    public void runEvery10Seconds()
    {
        log.info("Running bots every 10 second");
        for (var botName : botNames)
        {
            var botKingdom = kingdomRepository.getKingdomByName(botName);
            if (botKingdom.isEmpty())
            {
                log.error("kingdom {} not found", botName);
                return;
            }

            var kingdom = botKingdom.get();
            // TODO redo this funny code...
            Bot bot = switch (botName)
            {
                case "BlacksmithBot" -> new BlacksmithBot(kingdom, market);
                case "IronMinerBot" -> new IronMinerBot(kingdom, market);
                case "FarmerBot" -> new FarmerBot(kingdom, market);
                case "GoldMinerBot" -> new GoldMinerBot(kingdom, market);
                default -> new GoldMinerBot(kingdom, market);
            };

            runSingleBotActions(bot);
            log.info(bot.getKingdomInfo());
            log.info("[{}] actions done", botName);
        }
    }

    void runSingleBotActions(Bot bot)
    {
        runActions(bot);
        passTurn(bot);
    }

    @Transactional
    void runActions(Bot bot)
    {
        var kingdom = bot.getKingdom();
        bot.doAllActions();
        kingdomRepository.update(kingdom);
    }

    @Transactional
    void passTurn(Bot bot)
    {
        var kingdom = bot.getKingdom();
        if (bot.doesHaveEnoughUpkeep())
        {
            bot.passTurn();
        } else
        {
            log.info("[{}] didn't have at least 80% food upkeep for turn pass, skipping", kingdom.getName());
        }
        kingdomRepository.update(kingdom);
    }
}
