package com.knightsofdarkness.web.messaging.model;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationJpaRepository extends JpaRepository<NotificationEntity, UUID> {
    List<NotificationEntity> findByKingdomNameOrderByIsReadAscDateDesc(String kingdomName, Pageable pageable);

    long countByKingdomNameAndIsReadFalse(String kingdomName);
}
