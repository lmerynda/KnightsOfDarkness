package com.knightsofdarkness.web.bots;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.kingdom.IKingdomInteractor;
import com.knightsofdarkness.web.kingdom.IKingdomRepository;
import com.knightsofdarkness.web.market.IMarket;

import jakarta.transaction.Transactional;

@Component
public class BotsRunner {
    private final Logger log = LoggerFactory.getLogger(BotsRunner.class);

    private final IKingdomRepository kingdomRepository;
    private final IBotRepository botRepository;
    private final IMarket market;
    private final IKingdomInteractor kingdomInteractor;
    private final GameConfig gameConfig;

    public BotsRunner(IKingdomRepository kingdomRepository, IBotRepository botRepository, IMarket market, IKingdomInteractor kingdomInteractor, GameConfig gameConfig)
    {
        this.kingdomRepository = kingdomRepository;
        this.botRepository = botRepository;
        this.market = market;
        this.kingdomInteractor = kingdomInteractor;
        this.gameConfig = gameConfig;
    }

    // Runs every 10 seconds (units are in milliseconds)
    // @Scheduled(fixedRate = 1000 * 60 * 60)
    @Scheduled(fixedRate = 1000 * 60 * 60)
    @Transactional
    public void runEvery10Seconds()
    {
        log.info("Running bots every 10 second");
        var bots = botRepository.findAllWithKingdoms();
        for (var bot : bots)
        {
            var kingdom = bot.getKingdom();
            // TODO redo this funny code...
            IBot botRunner = switch (kingdom.getName())
            {
                case "BlacksmithBot" -> new BlacksmithBot(kingdom, market, kingdomInteractor, gameConfig);
                case "IronMinerBot" -> new IronMinerBot(kingdom, market, kingdomInteractor, gameConfig);
                case "FarmerBot" -> new FarmerBot(kingdom, market, kingdomInteractor, gameConfig);
                case "GoldMinerBot" -> new GoldMinerBot(kingdom, market, kingdomInteractor, gameConfig);
                default -> new GoldMinerBot(kingdom, market, kingdomInteractor, gameConfig);
            };

            runSingleBotActions(botRunner);
            log.info(botRunner.getKingdomInfo());
            log.info("[{}] actions done", kingdom.getName());
        }
    }

    void runSingleBotActions(IBot bot)
    {
        runActions(bot);
        passTurn(bot);
        kingdomRepository.update(bot.getKingdom());
    }

    boolean runActions(IBot bot)
    {
        var result = bot.doAllActions();
        return result;
    }

    void passTurn(IBot bot)
    {
        bot.passTurn();
    }
}
