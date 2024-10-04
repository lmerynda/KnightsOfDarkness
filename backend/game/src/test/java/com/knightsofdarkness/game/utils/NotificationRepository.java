package com.knightsofdarkness.game.utils;

import java.util.Optional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    public List<NotificationDto> findByKingdomNameOrderByIsReadAscDateDesc(String kingdomName, int limit)
    {
        return notifications.getOrDefault(kingdomName, new ArrayList<>()).stream()
                .sorted(Comparator.comparing(NotificationDto::isRead)
                        .thenComparing(Comparator.comparing(NotificationDto::date).reversed()))
                .limit(limit)
                .toList();
    }

    @Override
    public Optional<NotificationDto> findById(UUID notificationId)
    {
        return notifications.values().stream()
                .flatMap(List::stream)
                .filter(notification -> notification.id().equals(notificationId))
                .findFirst();
    }

    @Override
    public void update(NotificationDto updatedNotification)
    {
        notifications.values().stream()
                .flatMap(List::stream)
                .filter(notification -> notification.id().equals(updatedNotification.id()))
                .findFirst()
                .ifPresent(notification -> notification = updatedNotification);
    }

    @Override
    public long countKingdomUnreadNotifications(String kingdomName)
    {
        return notifications.getOrDefault(kingdomName, new ArrayList<>()).stream()
                .filter(notification -> !notification.isRead())
                .count();
    }
}
