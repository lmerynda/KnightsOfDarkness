package com.knightsofdarkness.web.bots;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.kingdom.IKingdomInteractor;
import com.knightsofdarkness.web.kingdom.IKingdomRepository;
import com.knightsofdarkness.web.market.IMarket;

@Service
public class BotsRunner {
    private final IKingdomRepository kingdomRepository;
    private final IMarket market;
    private final IKingdomInteractor kingdomInteractor;
    private final GameConfig gameConfig;

    public BotsRunner(IKingdomRepository kingdomRepository, IMarket market, IKingdomInteractor kingdomInteractor, GameConfig gameConfig)
    {
        this.market = market;
        this.kingdomInteractor = kingdomInteractor;
        this.gameConfig = gameConfig;
        this.kingdomRepository = kingdomRepository;
    }

    @Transactional
    void runSingleBotActions(String kingdomName)
    {
        var kingdom = kingdomRepository.getKingdomByName(kingdomName).orElseThrow();
        IBot botToRun = switch (kingdomName)
        {
            case "BlacksmithBot" -> new BlacksmithBot(kingdom, market, kingdomInteractor, gameConfig);
            case "IronMinerBot" -> new IronMinerBot(kingdom, market, kingdomInteractor, gameConfig);
            case "FarmerBot" -> new FarmerBot(kingdom, market, kingdomInteractor, gameConfig);
            case "GoldMinerBot" -> new GoldMinerBot(kingdom, market, kingdomInteractor, gameConfig);
            default -> new GoldMinerBot(kingdom, market, kingdomInteractor, gameConfig);
        };

        botToRun.doAllActions();
        botToRun.passTurn();
        kingdomRepository.update(kingdom);
    }
}
