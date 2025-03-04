package com.knightsofdarkness.web.messaging;

import java.util.List;
import java.util.UUID;

import com.knightsofdarkness.common.messaging.NotificationDto;

public interface INotificationSystem {
    void create(String kingdom, String message);

    List<NotificationDto> getNotifications(String kingdomName);

    void markNotificationAsRead(String kingdomName, UUID notificationId);

    long countKingdomUnreadNotifications(String kingdomName);
}
