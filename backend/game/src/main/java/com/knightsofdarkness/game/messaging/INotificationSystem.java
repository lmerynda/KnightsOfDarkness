package com.knightsofdarkness.game.messaging;

import java.util.List;

import com.knightsofdarkness.common.messaging.NotificationDto;

public interface INotificationSystem {
    void create(String kingdom, String message);

    List<NotificationDto> getNotifications(String kingdomName);
}
