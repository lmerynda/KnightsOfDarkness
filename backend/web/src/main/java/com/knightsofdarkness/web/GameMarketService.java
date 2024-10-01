package com.knightsofdarkness.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.game.market.IMarket;
import com.knightsofdarkness.game.market.Market;
import com.knightsofdarkness.game.messaging.INotificationSystem;
import com.knightsofdarkness.game.messaging.NotificationSystem;
import com.knightsofdarkness.game.storage.IKingdomRepository;
import com.knightsofdarkness.game.storage.IMarketOfferRepository;
import com.knightsofdarkness.game.storage.INotificationRepository;

@Service
@Configuration
public class GameMarketService {
    private final IMarketOfferRepository marketOfferRepository;
    private final IKingdomRepository kingdomRepository;
    private final GameConfig gameConfig;
    private final INotificationSystem notificationSystem;

    public GameMarketService(IMarketOfferRepository marketOfferRepository, IKingdomRepository kingdomRepository, INotificationRepository notificationRepository, GameConfig gameConfig)
    {
        this.marketOfferRepository = marketOfferRepository;
        this.kingdomRepository = kingdomRepository;
        this.gameConfig = gameConfig;

        this.notificationSystem = new NotificationSystem(notificationRepository);
    }

    @Bean
    public IMarket market()
    {
        return new Market(marketOfferRepository, kingdomRepository, notificationSystem, gameConfig);
    }

    @Bean
    INotificationSystem notificationSystem()
    {
        return notificationSystem;
    }
}
