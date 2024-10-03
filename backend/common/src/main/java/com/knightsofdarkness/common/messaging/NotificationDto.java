package com.knightsofdarkness.common.messaging;

import java.time.Instant;
import java.util.UUID;

public record NotificationDto(UUID id, String kingdomName, String message, Instant date, boolean isRead) {
}
