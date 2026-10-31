package com.knightsofdarkness.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import com.knightsofdarkness.web.kingdom.IKingdomInteractor;
import com.knightsofdarkness.web.kingdom.IKingdomRepository;
import com.knightsofdarkness.web.kingdom.model.KingdomInteractor;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.messaging.INotificationSystem;
import com.knightsofdarkness.web.war.IWarReportRepository;

@Service
@Configuration
public class KingdomInteractorService {
    private final IKingdomRepository kingdomRepository;
    private final GameConfig gameConfig;
    private final INotificationSystem notificationSystem;
    private final IWarReportRepository warReportRepository;

    public KingdomInteractorService(IKingdomRepository kingdomRepository, GameConfig gameConfig, INotificationSystem notificationSystem, IWarReportRepository warReportRepository)
    {
        this.kingdomRepository = kingdomRepository;
        this.gameConfig = gameConfig;
        this.notificationSystem = notificationSystem;
        this.warReportRepository = warReportRepository;
    }

    @Bean
    public IKingdomInteractor kingdomInteractor()
    {
        return new KingdomInteractor(kingdomRepository, gameConfig, notificationSystem, warReportRepository);
    }
}
