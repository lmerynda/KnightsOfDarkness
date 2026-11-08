package com.knightsofdarkness.web.era.model;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameEraJpaRepository extends JpaRepository<GameEraEntity, Integer> {
    Optional<GameEraEntity> findFirstByEndedAtIsNullOrderByNumberDesc();

    List<GameEraEntity> findByEndedAtIsNotNullOrderByNumberDesc();
}
