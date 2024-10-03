package com.knightsofdarkness.game.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.knightsofdarkness.common.messaging.NotificationDto;
import com.knightsofdarkness.game.storage.INotificationRepository;

public class NotificationRepository implements INotificationRepository {

    private final Map<String, List<NotificationDto>> notifications = new HashMap<>();

    @Override
    public void create(NotificationDto notification)
    {
        var kingdomName = notification.kingdomName();
        if (!notifications.containsKey(kingdomName))
        {
            notifications.put(kingdomName, new ArrayList<>());
        }

        notifications.get(kingdomName).add(notification);
    }

    @Override
    public List<NotificationDto> findByKingdom(String kingdomName, int limit)
    {
        return notifications.getOrDefault(kingdomName, new ArrayList<>()).stream().limit(limit).toList();
    }
}
