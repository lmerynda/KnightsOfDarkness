package com.knightsofdarkness.web.messaging.model;

import java.util.Optional;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.knightsofdarkness.web.common.messaging.NotificationDto;
import com.knightsofdarkness.web.messaging.INotificationRepository;

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
    public List<NotificationDto> findByKingdomNameOrderByIsReadAscDateDesc(String kingdomName, int limit)
    {
        PageRequest pageable = PageRequest.of(0, limit);
        var notifications = jpaRepository.findByKingdomNameOrderByIsReadAscDateDesc(kingdomName, pageable);
        return notifications.stream().map(NotificationEntity::toDto).toList();
    }

    @Override
    public Optional<NotificationDto> findById(UUID notificationId)
    {
        return jpaRepository.findById(notificationId).map(NotificationEntity::toDto);
    }

    @Override
    public void update(NotificationDto updatedNotification)
    {
        NotificationEntity entity = NotificationEntity.fromDto(updatedNotification);
        jpaRepository.save(entity);
    }

    @Override
    public long countKingdomUnreadNotifications(String kingdomName)
    {
        return jpaRepository.countByKingdomNameAndIsReadFalse(kingdomName);
    }
}
