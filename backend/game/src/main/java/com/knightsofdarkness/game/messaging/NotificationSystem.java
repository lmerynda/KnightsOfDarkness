package com.knightsofdarkness.game.messaging;

import java.time.Instant;
import java.util.List;

import com.knightsofdarkness.common.messaging.NotificationDto;
import com.knightsofdarkness.game.Id;
import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.game.storage.INotificationRepository;

public class NotificationSystem implements INotificationSystem {
    INotificationRepository notificationRepository;
    GameConfig gameConfig;

    public NotificationSystem(INotificationRepository notificationRepository, GameConfig gameConfig)
    {
        this.notificationRepository = notificationRepository;
        this.gameConfig = gameConfig;
    }

    @Override
    public void create(String kingdom, String message)
    {
        var NotificationDto = new NotificationDto(Id.generate(), kingdom, message, Instant.now(), false);
        notificationRepository.create(NotificationDto);
    }

    @Override
    public List<NotificationDto> getNotifications(String kingdomName)
    {
        return notificationRepository.findByKingdomNameOrderByIsReadAscDateDesc(kingdomName, gameConfig.common().maxNotificationsCount());
    }
}
