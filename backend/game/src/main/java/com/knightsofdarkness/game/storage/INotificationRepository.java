package com.knightsofdarkness.game.storage;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.knightsofdarkness.common.messaging.NotificationDto;

public interface INotificationRepository {
    void create(UUID id, String kingdomName, String message, Instant dateTime);

    List<NotificationDto> findByKingdom(String kingdomName);
}
