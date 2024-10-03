package com.knightsofdarkness.game.utils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.knightsofdarkness.common.messaging.NotificationDto;
import com.knightsofdarkness.game.storage.INotificationRepository;

public class NotificationRepository implements INotificationRepository {

    private final Map<String, List<NotificationDto>> notifications = new HashMap<>();

    @Override
    public void create(UUID id, String kingdomName, String message, Instant dateTime)
    {
        if (!notifications.containsKey(kingdomName))
        {
            notifications.put(kingdomName, new ArrayList<>());
        }

        notifications.get(kingdomName).add(new NotificationDto(id, kingdomName, message, dateTime));
    }

    @Override
    public List<NotificationDto> findByKingdom(String kingdomName, int limit)
    {
        return notifications.getOrDefault(kingdomName, new ArrayList<>()).stream().limit(limit).toList();
    }
}
