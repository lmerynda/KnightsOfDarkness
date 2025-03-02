package com.knightsofdarkness.web.messaging.legacy;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.knightsofdarkness.common.messaging.NotificationDto;

public class DummyNotificationSystem implements INotificationSystem {
    @Override
    public void create(String kingdom, String message)
    {
        // Do nothing
    }

    @Override
    public List<NotificationDto> getNotifications(String kingdomName)
    {
        return new ArrayList<>();
    }

    @Override
    public void markNotificationAsRead(String kingdomName, UUID notificationId)
    {
        // Do nothing
    }

    @Override
    public long countKingdomUnreadNotifications(String kingdomName)
    {
        return 0;
    }

}
