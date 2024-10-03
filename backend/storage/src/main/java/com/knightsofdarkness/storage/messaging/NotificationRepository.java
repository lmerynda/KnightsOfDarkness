package com.knightsofdarkness.storage.messaging;

import java.util.List;

import org.springframework.data.domain.PageRequest;
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
    public void create(NotificationDto notification)
    {
        NotificationEntity entity = NotificationEntity.fromDto(notification);
        jpaRepository.save(entity);
    }

    @Override
    public List<NotificationDto> findByKingdom(String kingdomName, int limit)
    {
        PageRequest pageable = PageRequest.of(0, limit);
        var notifications = jpaRepository.findByKingdomName(kingdomName, pageable);
        return notifications.stream().map(NotificationEntity::toDto).toList();
    }
}
