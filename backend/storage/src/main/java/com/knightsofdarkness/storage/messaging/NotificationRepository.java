package com.knightsofdarkness.storage.messaging;

import java.time.Instant;

import org.springframework.stereotype.Repository;

import com.knightsofdarkness.game.Id;
import com.knightsofdarkness.game.storage.INotificationRepository;

@Repository
public class NotificationRepository implements INotificationRepository {
    private final NotificationJpaRepository jpaRepository;

    public NotificationRepository(NotificationJpaRepository jpaRepository)
    {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void create(String kingdomName, String message)
    {
        NotificationEntity entity = new NotificationEntity(Id.generate(), kingdomName, message, Instant.now());
        jpaRepository.save(entity);
    }
}
