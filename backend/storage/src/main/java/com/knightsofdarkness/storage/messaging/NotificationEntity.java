package com.knightsofdarkness.storage.messaging;

import java.time.Instant;
import java.util.UUID;

import com.knightsofdarkness.common.messaging.NotificationDto;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class NotificationEntity {
    @Id
    private UUID id;
    private String kingdomName;
    private String message;
    private Instant date;

    public NotificationEntity()
    {
    }

    public NotificationEntity(UUID id, String kingdomName, String message, Instant date)
    {
        this.id = id;
        this.kingdomName = kingdomName;
        this.message = message;
        this.date = date;
    }

    public NotificationDto toDto()
    {
        return new NotificationDto(id, kingdomName, message, date);
    }
}
