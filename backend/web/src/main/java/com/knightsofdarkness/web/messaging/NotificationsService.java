package com.knightsofdarkness.web.messaging;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.knightsofdarkness.common.messaging.NotificationDto;
import com.knightsofdarkness.game.messaging.INotificationSystem;

import jakarta.transaction.Transactional;

@Service
public class NotificationsService {
    private static final Logger log = LoggerFactory.getLogger(NotificationsService.class);
    INotificationSystem notificationSystem;

    public NotificationsService(INotificationSystem notificationSystem)
    {
        this.notificationSystem = notificationSystem;
    }

    public List<NotificationDto> getNotifications(String kingdomName)
    {
        log.info("Getting all offers");
        return notificationSystem.getNotifications(kingdomName);
    }

    @Transactional
    public void markNotificationAsRead(String kingdomName, UUID notificationId)
    {
        log.info("Marking notification as read");
        notificationSystem.markNotificationAsRead(kingdomName, notificationId);
    }
}
