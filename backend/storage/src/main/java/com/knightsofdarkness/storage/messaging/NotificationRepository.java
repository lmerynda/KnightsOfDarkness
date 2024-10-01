package com.knightsofdarkness.storage.messaging;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.knightsofdarkness.common.messaging.NotificationDto;
import com.knightsofdarkness.game.storage.INotificationRepository;

@Repository
public class NotificationRepository implements INotificationRepository {
    private final NotificationJpaRepository jpaRepository;

    public NotificationRepository(NotificationJpaRepository jpaRepository)
    {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void create(UUID id, String kingdomName, String message, Instant dateTime)
    {
        NotificationEntity entity = new NotificationEntity(id, kingdomName, message, dateTime);
        jpaRepository.save(entity);
    }

    @Override
    public List<NotificationDto> findByKingdom(String kingdomName)
    {
        var notifications = jpaRepository.findByKingdomName(kingdomName);
        return notifications.stream().map(NotificationEntity::toDto).toList();
    }
}
