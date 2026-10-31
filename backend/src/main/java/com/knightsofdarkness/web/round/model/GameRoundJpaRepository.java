package com.knightsofdarkness.web.round.model;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRoundJpaRepository extends JpaRepository<GameRoundEntity, Integer> {
    Optional<GameRoundEntity> findFirstByEndedAtIsNullOrderByNumberDesc();

    List<GameRoundEntity> findByEndedAtIsNotNullOrderByNumberDesc();
}
