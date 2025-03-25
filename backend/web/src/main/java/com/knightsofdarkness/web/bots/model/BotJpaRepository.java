package com.knightsofdarkness.web.bots.model;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BotJpaRepository extends JpaRepository<BotEntity, UUID> {

    @Query("SELECT bot FROM BotEntity bot JOIN FETCH bot.kingdom")
    List<BotEntity> findAllWithKingdoms();

}
