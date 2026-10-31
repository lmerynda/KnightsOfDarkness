package com.knightsofdarkness.web.common.messaging;

import java.time.Instant;
import java.util.UUID;

public record NotificationDto(UUID id, String kingdomName, NotificationKind kind, String message, Instant date, boolean isRead) {
}
