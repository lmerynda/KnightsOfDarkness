package com.knightsofdarkness.game.storage;

import java.util.List;

import com.knightsofdarkness.common.messaging.NotificationDto;

public interface INotificationRepository {
    void create(NotificationDto notification);

    public List<NotificationDto> findByKingdom(String kingdomName, int limit);
}
