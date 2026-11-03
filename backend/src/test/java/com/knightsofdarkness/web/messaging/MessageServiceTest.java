package com.knightsofdarkness.web.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.knightsofdarkness.web.GameServerTest;

@GameServerTest
class MessageServiceTest {
    // the game initializer creates uprzejmy and Umbar, both in the Legion alliance, plus four bots outside of it
    @Autowired
    private MessageService messageService;

    @Test
    void directMessage_shouldReachRecipientInboxAndSenderOutbox()
    {
        var unreadBefore = messageService.countUnread("Umbar");

        var result = messageService.send("uprzejmy", "Umbar", "  Meet me at the river  ");

        assertTrue(result.success());
        var inbox = messageService.getInbox("Umbar");
        assertEquals("Meet me at the river", inbox.get(0).body());
        assertEquals("uprzejmy", inbox.get(0).sender());
        assertFalse(inbox.get(0).toAlliance());
        assertEquals(unreadBefore + 1, messageService.countUnread("Umbar"));
        assertEquals("Meet me at the river", messageService.getSent("uprzejmy").get(0).body());
    }

    @Test
    void invalidMessages_shouldBeRejected()
    {
        assertFalse(messageService.send("uprzejmy", "Umbar", "   ").success());
        assertFalse(messageService.send("uprzejmy", "uprzejmy", "hello me").success());
        assertFalse(messageService.send("uprzejmy", "Nowhere", "hello").success());
        assertFalse(messageService.send("uprzejmy", "Umbar", "x".repeat(MessageService.maxBodyLength + 1)).success());
    }

    @Test
    void allianceMessage_shouldReachEveryOtherMember()
    {
        var result = messageService.sendToAlliance("Umbar", "Rally at dawn");

        assertTrue(result.success());
        assertEquals(1, result.data().orElseThrow());
        var received = messageService.getInbox("uprzejmy").get(0);
        assertEquals("Rally at dawn", received.body());
        assertTrue(received.toAlliance());
    }

    @Test
    void allianceMessage_withoutAlliance_shouldFail()
    {
        assertFalse(messageService.sendToAlliance("FarmerBot", "anyone?").success());
    }

    @Test
    void markingAsRead_shouldOnlyWorkForRecipient()
    {
        messageService.send("Umbar", "uprzejmy", "secret");
        var message = messageService.getInbox("uprzejmy").get(0);

        assertFalse(messageService.markAsRead("Umbar", message.id()));
        assertTrue(messageService.markAsRead("uprzejmy", message.id()));
        assertTrue(messageService.getInbox("uprzejmy").get(0).isRead());
    }
}
