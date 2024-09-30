package com.knightsofdarkness.storage.messaging;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class NotificationEntity {
    @Id
    private UUID id;
    private String kingdom;
    private String message;
    private Instant date;

    public NotificationEntity()
    {
    }

    public NotificationEntity(UUID id, String kingdom, String message, Instant date)
    {
        this.id = id;
        this.kingdom = kingdom;
        this.message = message;
        this.date = date;
    }
}
