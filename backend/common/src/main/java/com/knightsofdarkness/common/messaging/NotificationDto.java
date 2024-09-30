package com.knightsofdarkness.common.messaging;

import java.time.Instant;
import java.util.UUID;

public class NotificationDto {
    private UUID id;
    private String kingdom;
    private String message;
    private Instant date;

    public NotificationDto()
    {
    }

    public NotificationDto(UUID id, String kingdom, String message, Instant date)
    {
        this.id = id;
        this.kingdom = kingdom;
        this.message = message;
        this.date = date;
    }
}
