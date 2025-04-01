package com.knightsofdarkness.web.bots.model;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BotJpaRepository extends JpaRepository<BotEntity, UUID> {

}
