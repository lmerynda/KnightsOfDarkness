package com.knightsofdarkness.game.messaging;

import java.time.Instant;
import java.util.List;

import com.knightsofdarkness.common.messaging.NotificationDto;
import com.knightsofdarkness.game.Id;
import com.knightsofdarkness.game.storage.INotificationRepository;

public class NotificationSystem implements INotificationSystem {
    INotificationRepository notificationRepository;

    public NotificationSystem(INotificationRepository notificationRepository)
    {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void create(String kingdom, String message)
    {
        notificationRepository.create(Id.generate(), kingdom, message, Instant.now());
    }

    @Override
    public List<NotificationDto> getNotifications(String kingdomName)
    {
        int limit = 15;
        return notificationRepository.findByKingdom(kingdomName, limit);
    }
}
