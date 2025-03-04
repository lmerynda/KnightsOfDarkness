package com.knightsofdarkness.web;

import com.knightsofdarkness.web.Game;
import com.knightsofdarkness.web.kingdom.model.KingdomInteractor;
import com.knightsofdarkness.web.messaging.legacy.DummyNotificationSystem;
import com.knightsofdarkness.web.utils.KingdomRepository;
import com.knightsofdarkness.web.utils.MarketBuilder;
import com.knightsofdarkness.web.utils.NotificationRepository;

import net.bytebuddy.build.AccessControllerPlugin.Initializer;

public class TestGame {
    public Game get()
    {
        var config = Initializer.readGameConfig();
        var kingdomRepository = new KingdomRepository();
        var notificationRepository = new NotificationRepository();
        var notificationSystem = new DummyNotificationSystem();
        var market = new MarketBuilder(config, notificationSystem).withKingdomRepository(kingdomRepository).build();
        var kingdomInteractor = new KingdomInteractor(kingdomRepository);

        return new Game(config, market, kingdomRepository, notificationSystem, kingdomInteractor);
    }
}
