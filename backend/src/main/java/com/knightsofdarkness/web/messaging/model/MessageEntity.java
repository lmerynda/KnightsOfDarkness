package com.knightsofdarkness.web.messaging.model;

import java.time.Instant;
import java.util.UUID;

import com.knightsofdarkness.web.common.messaging.MessageDto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class MessageEntity {
    @Id
    UUID id;
    String sender;
    String recipient;

    @Column(length = 2000)
    String body;

    Instant date;
    boolean isRead;
    boolean toAlliance;

    public MessageEntity()
    {
    }

    public MessageEntity(UUID id, String sender, String recipient, String body, Instant date, boolean toAlliance)
    {
        this.id = id;
        this.sender = sender;
        this.recipient = recipient;
        this.body = body;
        this.date = date;
        this.toAlliance = toAlliance;
    }

    public MessageDto toDto()
    {
        return new MessageDto(id, sender, recipient, body, date, isRead, toAlliance);
    }

    public String getRecipient()
    {
        return recipient;
    }

    public void markAsRead()
    {
        isRead = true;
    }
}
