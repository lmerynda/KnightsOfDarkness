package com.knightsofdarkness.game;

import com.knightsofdarkness.game.gameconfig.Initializer;
import com.knightsofdarkness.game.interactions.KingdomInteractor;
import com.knightsofdarkness.game.messaging.NotificationSystem;
import com.knightsofdarkness.game.utils.KingdomRepository;
import com.knightsofdarkness.game.utils.MarketBuilder;
import com.knightsofdarkness.game.utils.NotificationRepository;

public class TestGame {
    public Game get()
    {
        var config = Initializer.readGameConfig();
        var kingdomRepository = new KingdomRepository();
        var notificationRepository = new NotificationRepository();
        var notificationSystem = new NotificationSystem(notificationRepository, config);
        var market = new MarketBuilder(config, notificationSystem).withKingdomRepository(kingdomRepository).build();
        var kingdomInteractor = new KingdomInteractor(kingdomRepository);

        return new Game(config, market, kingdomRepository, notificationSystem, kingdomInteractor);
    }
}
