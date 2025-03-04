package com.knightsofdarkness.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.kingdom.IKingdomRepository;
import com.knightsofdarkness.web.market.IMarket;
import com.knightsofdarkness.web.market.model.IMarketOfferRepository;
import com.knightsofdarkness.web.market.model.Market;
import com.knightsofdarkness.web.messaging.INotificationRepository;
import com.knightsofdarkness.web.messaging.INotificationSystem;
import com.knightsofdarkness.web.messaging.NotificationSystem;

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

        this.notificationSystem = new NotificationSystem(notificationRepository, gameConfig);
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
