package com.knightsofdarkness.web.legacy;

import com.knightsofdarkness.web.Game;
import com.knightsofdarkness.web.game.config.Initializer;
import com.knightsofdarkness.web.kingdom.model.KingdomInteractor;
import com.knightsofdarkness.web.messaging.legacy.DummyNotificationSystem;
import com.knightsofdarkness.web.utils.KingdomRepository;
import com.knightsofdarkness.web.utils.MarketBuilder;

public class TestGame {
    public Game get()
    {
        var config = Initializer.readGameConfig();
        var kingdomRepository = new KingdomRepository();
        var notificationSystem = new DummyNotificationSystem();
        var market = new MarketBuilder(config, notificationSystem).withKingdomRepository(kingdomRepository).build();
        var kingdomInteractor = new KingdomInteractor(kingdomRepository);

        return new Game(config, market, kingdomRepository, notificationSystem, kingdomInteractor);
    }
}
