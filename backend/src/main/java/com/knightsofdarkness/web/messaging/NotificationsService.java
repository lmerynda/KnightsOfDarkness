package com.knightsofdarkness.web.messaging;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.knightsofdarkness.web.common.messaging.NotificationDto;

@Service
public class NotificationsService {
    private static final Logger log = LoggerFactory.getLogger(NotificationsService.class);
    INotificationSystem notificationSystem;

    public NotificationsService(INotificationSystem notificationSystem)
    {
        this.notificationSystem = notificationSystem;
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getNotifications(String kingdomName)
    {
        log.info("Getting all notifications for kingdom {}", kingdomName);
        return notificationSystem.getNotifications(kingdomName);
    }

    @Transactional
    public void markNotificationAsRead(String kingdomName, UUID notificationId)
    {
        log.info("Marking notification as read");
        notificationSystem.markNotificationAsRead(kingdomName, notificationId);
    }

    @Transactional(readOnly = true)
    public long countKingdomUnreadNotifications(String kingdomName)
    {
        log.info("Counting unread notifications for kingdom {}", kingdomName);
        return notificationSystem.countKingdomUnreadNotifications(kingdomName);
    }
}
