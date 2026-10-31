package com.knightsofdarkness.web.messaging;

import java.util.List;
import java.util.UUID;

import com.knightsofdarkness.web.common.messaging.NotificationDto;
import com.knightsofdarkness.web.common.messaging.NotificationKind;

public interface INotificationSystem {
    void create(String kingdom, NotificationKind kind, String message);

    List<NotificationDto> getNotifications(String kingdomName);

    void markNotificationAsRead(String kingdomName, UUID notificationId);

    long countKingdomUnreadNotifications(String kingdomName);
}
