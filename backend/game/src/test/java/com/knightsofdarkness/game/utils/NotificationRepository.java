package com.knightsofdarkness.game.utils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.knightsofdarkness.common.messaging.NotificationDto;
import com.knightsofdarkness.game.Id;
import com.knightsofdarkness.game.storage.INotificationRepository;

public class NotificationRepository implements INotificationRepository {

    private final Map<String, List<NotificationDto>> notifications = new HashMap<>();

    @Override
    public void create(String kingdomName, String message)
    {
        if (!notifications.containsKey(kingdomName))
        {
            notifications.put(kingdomName, new ArrayList<>());
        }

        notifications.get(kingdomName).add(new NotificationDto(Id.generate(), kingdomName, message, Instant.now()));
    }
}
