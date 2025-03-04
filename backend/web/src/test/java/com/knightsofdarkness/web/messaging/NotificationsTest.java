package com.knightsofdarkness.web.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.web.Game;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;
import com.knightsofdarkness.web.legacy.TestGame;
import com.knightsofdarkness.web.messaging.legacy.INotificationSystem;
import com.knightsofdarkness.web.utils.KingdomBuilder;

class NotificationsTest {
    private Game game;
    private KingdomEntity kingdom;
    private INotificationSystem notificationSystem;

    @BeforeEach
    void beforeEach()
    {
        game = new TestGame().get();
        kingdom = new KingdomBuilder(game).build();
        game.addKingdom(kingdom);
        notificationSystem = game.getNotificationSystem();
    }

    @Test
    void testAddNotification()
    {
        notificationSystem.create(kingdom.getName(), "Test notification");

        assertEquals(1, notificationSystem.getNotifications(kingdom.getName()).size());
    }

    @Test
    void testMarkNotificationAsRead()
    {
        notificationSystem.create(kingdom.getName(), "Test notification");
        var notifications = notificationSystem.getNotifications(kingdom.getName()).getFirst();
        var notificationId = notifications.id();

        notificationSystem.markNotificationAsRead(kingdom.getName(), notificationId);

        var updatedNotifications = notificationSystem.getNotifications(kingdom.getName());
        assertTrue(updatedNotifications.get(0).isRead());
    }

    @Test
    void testCountKingdomUnreadNotifications()
    {
        notificationSystem.create(kingdom.getName(), "Test notification 1");
        notificationSystem.create(kingdom.getName(), "Test notification 2");

        assertEquals(2, notificationSystem.countKingdomUnreadNotifications(kingdom.getName()));

        var notifications = notificationSystem.getNotifications(kingdom.getName());
        notificationSystem.markNotificationAsRead(kingdom.getName(), notifications.get(0).id());

        assertEquals(1, notificationSystem.countKingdomUnreadNotifications(kingdom.getName()));
    }

    @Test
    void testGetNotificationsOrder()
    {
        notificationSystem.create(kingdom.getName(), "First notification");
        notificationSystem.create(kingdom.getName(), "Second notification");

        var notifications = notificationSystem.getNotifications(kingdom.getName());

        assertEquals("Second notification", notifications.get(0).message());
        assertEquals("First notification", notifications.get(1).message());
    }

    @Test
    void testMarkNotificationAsReadInvalidKingdom()
    {
        notificationSystem.create(kingdom.getName(), "Test notification");
        var notifications = notificationSystem.getNotifications(kingdom.getName());
        var notificationId = notifications.get(0).id();

        notificationSystem.markNotificationAsRead("InvalidKingdom", notificationId);

        var updatedNotifications = notificationSystem.getNotifications(kingdom.getName());
        assertFalse(updatedNotifications.get(0).isRead());
    }

    @Test
    void testMarkNotificationAsReadInvalidId()
    {
        notificationSystem.create(kingdom.getName(), "Test notification");

        notificationSystem.markNotificationAsRead(kingdom.getName(), UUID.randomUUID());

        var notifications = notificationSystem.getNotifications(kingdom.getName());
        assertFalse(notifications.get(0).isRead());
    }
}
