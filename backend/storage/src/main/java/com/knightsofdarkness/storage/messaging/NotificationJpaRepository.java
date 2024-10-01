package com.knightsofdarkness.storage.messaging;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.knightsofdarkness.common.messaging.NotificationDto;

@Repository
public interface NotificationJpaRepository extends JpaRepository<NotificationEntity, UUID> {
    List<NotificationDto> findByKingdomName(String kingdomName);
}
