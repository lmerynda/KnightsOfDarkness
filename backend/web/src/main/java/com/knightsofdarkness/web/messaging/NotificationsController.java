package com.knightsofdarkness.web.messaging;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.knightsofdarkness.common.messaging.NotificationDto;
import com.knightsofdarkness.web.market.MarketController;
import com.knightsofdarkness.web.user.UserData;

@RestController
public class NotificationsController {
    private static final Logger log = LoggerFactory.getLogger(MarketController.class);
    private final NotificationsService notificationsService;

    public NotificationsController(NotificationsService notificationsService)
    {
        this.notificationsService = notificationsService;
    }

    @GetMapping("/kingdom/notifications")
    List<NotificationDto> getNotifications(@AuthenticationPrincipal UserData currentUser)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return new ArrayList<>(); // TODO change into ResponseEntity
        }

        return notificationsService.getNotifications(currentUser.getKingdomName());
    }

    @PostMapping("/kingdom/notifications/{id}/mark-as-read")
    void markNotificationAsRead(@AuthenticationPrincipal UserData currentUser, @PathVariable UUID id)
    {
        if (currentUser == null)
        {
            logUserUnauthenticated();
            return;
        }

        notificationsService.markNotificationAsRead(currentUser.getKingdomName(), id);
    }

    private void logUserUnauthenticated()
    {
        log.error("User not read from authentication context");
    }
}
