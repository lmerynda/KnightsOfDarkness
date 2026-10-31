package com.knightsofdarkness.web.common.messaging;

import java.time.Instant;
import java.util.UUID;

public record MessageDto(UUID id, String sender, String recipient, String body, Instant date, boolean isRead, boolean toAlliance) {
}
