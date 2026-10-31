package com.knightsofdarkness.web.messaging.model;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageJpaRepository extends JpaRepository<MessageEntity, UUID> {
    List<MessageEntity> findByRecipientOrderByDateDesc(String recipient, Pageable pageable);

    List<MessageEntity> findBySenderOrderByDateDesc(String sender, Pageable pageable);

    long countByRecipientAndIsReadFalse(String recipient);
}
