package com.knightsofdarkness.web.market.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.knightsofdarkness.common.market.MarketResource;

@Repository
public interface MarketTransactionJpaRepository extends JpaRepository<MarketTransactionEntity, UUID> {
    @Query("SELECT t FROM MarketTransactionEntity t WHERE t.resource = :resource AND t.date >= :startTime AND t.date < :endTime ORDER BY t.date DESC")
    List<MarketTransactionEntity> findTransactionsForResourceAndTimeRange(
            @Param("resource") MarketResource resource,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime);
}
