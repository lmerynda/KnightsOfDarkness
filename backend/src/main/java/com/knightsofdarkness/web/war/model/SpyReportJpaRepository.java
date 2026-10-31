package com.knightsofdarkness.web.war.model;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpyReportJpaRepository extends JpaRepository<SpyReportEntity, UUID> {
    List<SpyReportEntity> findByKingdomNameOrderByDateDesc(String kingdomName, Pageable pageable);
}
