package com.knightsofdarkness.web.messaging.model;

import java.time.Instant;
import java.util.UUID;

import com.knightsofdarkness.web.common.messaging.NotificationDto;
import com.knightsofdarkness.web.common.messaging.NotificationKind;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

@Entity
public class NotificationEntity {
    @Id
    private UUID id;
    private String kingdomName;
    @Enumerated(EnumType.STRING)
    private NotificationKind kind;
    private String message;
    private Instant date;
    private boolean isRead;

    public NotificationEntity()
    {
    }

    public NotificationEntity(UUID id, String kingdomName, NotificationKind kind, String message, Instant date, boolean isRead)
    {
        this.id = id;
        this.kingdomName = kingdomName;
        this.kind = kind;
        this.message = message;
        this.date = date;
        this.isRead = isRead;
    }

    public NotificationDto toDto()
    {
        return new NotificationDto(id, kingdomName, kind, message, date, isRead);
    }

    public static NotificationEntity fromDto(NotificationDto dto)
    {
        return new NotificationEntity(dto.id(), dto.kingdomName(), dto.kind(), dto.message(), dto.date(), dto.isRead());
    }
}
