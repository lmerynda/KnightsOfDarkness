package com.knightsofdarkness.web.bots;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.knightsofdarkness.game.bot.IBot;
import com.knightsofdarkness.game.interactions.IKingdomInteractor;
import com.knightsofdarkness.game.market.IMarket;
import com.knightsofdarkness.web.kingdom.model.KingdomRepository;

@Component
public class BotsRunner {
    private final Logger log = LoggerFactory.getLogger(BotsRunner.class);

    private final KingdomRepository kingdomRepository;
    private final IMarket market;
    private final IKingdomInteractor kingdomInteractor;
    private final List<String> botNames = Arrays.asList("BlacksmithBot", "FarmerBot", "IronMinerBot", "GoldMinerBot");

    public BotsRunner(KingdomRepository kingdomRepository, IMarket market, IKingdomInteractor kingdomInteractor)
    {
        this.kingdomRepository = kingdomRepository;
        this.market = market;
        this.kingdomInteractor = kingdomInteractor;
    }

    // // Runs every 10 seconds (units are in milliseconds)
    // @Scheduled(fixedRate = 1000 * 60 * 60)
    // @Transactional
    // public void runEvery10Seconds()
    // {
    // log.info("Running bots every 10 second");
    // for (var botName : botNames)
    // {
    // var botKingdom = kingdomRepository.getKingdomByName(botName);
    // if (botKingdom.isEmpty())
    // {
    // log.error("kingdom {} not found", botName);
    // return;
    // }

    // var kingdom = botKingdom.get();
    // // TODO redo this funny code...
    // IBot bot = switch (botName)
    // {
    // case "BlacksmithBot" -> new BlacksmithBot(kingdom, market, kingdomInteractor);
    // case "IronMinerBot" -> new IronMinerBot(kingdom, market, kingdomInteractor);
    // case "FarmerBot" -> new FarmerBot(kingdom, market, kingdomInteractor);
    // case "GoldMinerBot" -> new GoldMinerBot(kingdom, market, kingdomInteractor);
    // default -> new GoldMinerBot(kingdom, market, kingdomInteractor);
    // };

    // runSingleBotActions(bot);
    // log.info(bot.getKingdomInfo());
    // log.info("[{}] actions done", botName);
    // }
    // }

    void runSingleBotActions(IBot bot)
    {
        runActions(bot);
        passTurn(bot);
    }

    boolean runActions(IBot bot)
    {
        var kingdom = bot.getKingdom();
        var result = bot.doAllActions();
        // kingdomRepository.update(kingdom);
        return result;
    }

    void passTurn(IBot bot)
    {
        var kingdom = bot.getKingdom();
        if (kingdom.hasMaxTurns() || bot.doesHaveEnoughUpkeep())
        {
            bot.passTurn();
        } else
        {
            log.info("[{}] didn't have at least 80% upkeep for turn pass, skipping", kingdom.getName());
        }
        // kingdomRepository.update(kingdom);
    }
}
