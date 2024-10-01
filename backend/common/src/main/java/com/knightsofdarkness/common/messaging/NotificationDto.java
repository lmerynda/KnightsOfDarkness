package com.knightsofdarkness.common.messaging;

import java.time.Instant;
import java.util.UUID;

public record NotificationDto(UUID id, String kingdom, String message, Instant date) {
}
