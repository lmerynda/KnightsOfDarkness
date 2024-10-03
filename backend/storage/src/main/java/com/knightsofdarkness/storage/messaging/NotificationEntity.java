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
    private boolean isRead;

    public NotificationEntity()
    {
    }

    public NotificationEntity(UUID id, String kingdomName, String message, Instant date, boolean isRead)
    {
        this.id = id;
        this.kingdomName = kingdomName;
        this.message = message;
        this.date = date;
        this.isRead = isRead;
    }

    public NotificationDto toDto()
    {
        return new NotificationDto(id, kingdomName, message, date, isRead);
    }

    public static NotificationEntity fromDto(NotificationDto dto)
    {
        return new NotificationEntity(dto.id(), dto.kingdomName(), dto.message(), dto.date(), dto.isRead());
    }
}
