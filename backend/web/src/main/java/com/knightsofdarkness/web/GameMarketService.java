package com.knightsofdarkness.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.game.market.IMarket;
import com.knightsofdarkness.game.market.Market;
import com.knightsofdarkness.game.storage.IKingdomRepository;
import com.knightsofdarkness.game.storage.IMarketOfferRepository;
import com.knightsofdarkness.game.storage.INotificationRepository;

@Service
@Configuration
public class GameMarketService {
    private final IMarketOfferRepository marketOfferRepository;
    private final IKingdomRepository kingdomRepository;
    private final INotificationRepository notificationRepository;
    private final GameConfig gameConfig;

    public GameMarketService(IMarketOfferRepository marketOfferRepository, IKingdomRepository kingdomRepository, INotificationRepository notificationRepository, GameConfig gameConfig)
    {
        this.marketOfferRepository = marketOfferRepository;
        this.kingdomRepository = kingdomRepository;
        this.notificationRepository = notificationRepository;
        this.gameConfig = gameConfig;
    }

    @Bean
    public IMarket market()
    {
        return new Market(marketOfferRepository, kingdomRepository, notificationRepository, gameConfig);
    }
}
