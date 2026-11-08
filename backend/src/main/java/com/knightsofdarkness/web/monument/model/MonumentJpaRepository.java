package com.knightsofdarkness.web.monument.model;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

/**
 * Members pay in while the clock takes the upkeep, so whoever changes a monument holds its row until the transaction ends.
 */
@Repository
public interface MonumentJpaRepository extends JpaRepository<MonumentEntity, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM MonumentEntity m WHERE m.allianceName = :allianceName")
    Optional<MonumentEntity> findByAllianceNameForUpdate(String allianceName);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM MonumentEntity m ORDER BY m.allianceName")
    List<MonumentEntity> findAllForUpdate();
}
