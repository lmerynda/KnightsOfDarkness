package com.knightsofdarkness.web.alliance.model;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

@Repository
public interface AllianceJpaRepository extends JpaRepository<AllianceEntity, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AllianceEntity a WHERE a.name = :name")
    Optional<AllianceEntity> findByNameForUpdate(String name);
}
