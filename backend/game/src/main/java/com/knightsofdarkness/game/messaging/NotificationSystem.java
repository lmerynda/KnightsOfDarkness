package com.knightsofdarkness.game.messaging;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

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

    @Override
    public void markNotificationAsRead(String kingdomName, UUID notificationId)
    {
        var maybeNotification = notificationRepository.findById(notificationId);
        if (maybeNotification.isEmpty())
        {
            return;
        }

        var notification = maybeNotification.get();
        if (!notification.kingdomName().equals(kingdomName))
        {
            return;
        }

        var updatedNotification = new NotificationDto(notification.id(), notification.kingdomName(), notification.message(), notification.date(), true);

        notificationRepository.update(updatedNotification);
    }
}
