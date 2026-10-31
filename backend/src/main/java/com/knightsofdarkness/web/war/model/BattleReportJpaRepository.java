package com.knightsofdarkness.web.war.model;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BattleReportJpaRepository extends JpaRepository<BattleReportEntity, UUID> {
    @Query("SELECT r FROM BattleReportEntity r WHERE r.attacker = :kingdomName OR r.defender = :kingdomName ORDER BY r.date DESC")
    List<BattleReportEntity> findByKingdom(String kingdomName, Pageable pageable);
}
