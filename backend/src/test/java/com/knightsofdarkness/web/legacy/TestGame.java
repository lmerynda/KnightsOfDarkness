package com.knightsofdarkness.web.legacy;

import com.knightsofdarkness.web.Game;
import com.knightsofdarkness.web.game.config.Initializer;
import com.knightsofdarkness.web.kingdom.model.KingdomInteractor;
import com.knightsofdarkness.web.messaging.NotificationSystem;
import com.knightsofdarkness.web.utils.AllianceRepository;
import com.knightsofdarkness.web.utils.BotRepository;
import com.knightsofdarkness.web.utils.KingdomRepository;
import com.knightsofdarkness.web.utils.MarketBuilder;
import com.knightsofdarkness.web.utils.NotificationRepository;
import com.knightsofdarkness.web.utils.WarReportRepository;

public class TestGame {
    public Game get()
    {
        var config = Initializer.readGameConfig();
        var kingdomRepository = new KingdomRepository();
        var allianceRepository = new AllianceRepository();
        var botRepository = new BotRepository();
        var warReportRepository = new WarReportRepository();
        var notificationSystem = new NotificationSystem(new NotificationRepository(), config);
        var market = new MarketBuilder(config, notificationSystem).withKingdomRepository(kingdomRepository).build();
        var kingdomInteractor = new KingdomInteractor(kingdomRepository, config, notificationSystem, warReportRepository);

        return new Game(config, market, kingdomRepository, allianceRepository, botRepository, notificationSystem, kingdomInteractor, warReportRepository);
    }
}
