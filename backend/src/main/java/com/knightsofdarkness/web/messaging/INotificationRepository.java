package com.knightsofdarkness.web.messaging;

import java.util.Optional;

import java.util.List;
import java.util.UUID;

import com.knightsofdarkness.web.common.messaging.NotificationDto;

public interface INotificationRepository {
    List<NotificationDto> findByKingdomNameOrderByIsReadAscDateDesc(String kingdomName, int limit);

    Optional<NotificationDto> findById(UUID notificationId);

    void create(NotificationDto notification);

    void update(NotificationDto updatedNotification);

    long countKingdomUnreadNotifications(String kingdomName);
}
