package com.knightsofdarkness.web.bots;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.kingdom.IKingdomInteractor;
import com.knightsofdarkness.web.kingdom.model.KingdomRepository;
import com.knightsofdarkness.web.market.IMarket;

import jakarta.transaction.Transactional;

@Component
public class BotsRunner {
    private final Logger log = LoggerFactory.getLogger(BotsRunner.class);

    private final KingdomRepository kingdomRepository;
    private final IMarket market;
    private final IKingdomInteractor kingdomInteractor;
    private final List<String> botNames = Arrays.asList("BlacksmithBot", "FarmerBot", "IronMinerBot", "GoldMinerBot");
    private final GameConfig gameConfig;

    public BotsRunner(KingdomRepository kingdomRepository, IMarket market, IKingdomInteractor kingdomInteractor, GameConfig gameConfig)
    {
        this.kingdomRepository = kingdomRepository;
        this.market = market;
        this.kingdomInteractor = kingdomInteractor;
        this.gameConfig = gameConfig;
    }

    // Runs every 10 seconds (units are in milliseconds)
    @Scheduled(fixedRate = 1000 * 60 * 60)
    @Transactional
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
            IBot bot = switch (botName)
            {
                case "BlacksmithBot" -> new BlacksmithBot(kingdom, market, kingdomInteractor, gameConfig);
                case "IronMinerBot" -> new IronMinerBot(kingdom, market, kingdomInteractor, gameConfig);
                case "FarmerBot" -> new FarmerBot(kingdom, market, kingdomInteractor, gameConfig);
                case "GoldMinerBot" -> new GoldMinerBot(kingdom, market, kingdomInteractor, gameConfig);
                default -> new GoldMinerBot(kingdom, market, kingdomInteractor, gameConfig);
            };

            runSingleBotActions(bot);
            log.info(bot.getKingdomInfo());
            log.info("[{}] actions done", botName);
        }
    }

    void runSingleBotActions(IBot bot)
    {
        runActions(bot);
        passTurn(bot);
    }

    boolean runActions(IBot bot)
    {
        var result = bot.doAllActions();
        // kingdomRepository.update(kingdom);
        return result;
    }

    void passTurn(IBot bot)
    {
        bot.passTurn();
    }
}
