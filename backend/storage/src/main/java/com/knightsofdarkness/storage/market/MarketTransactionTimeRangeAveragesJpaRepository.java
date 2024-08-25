package com.knightsofdarkness.storage.market;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.knightsofdarkness.game.market.MarketResource;

public interface MarketTransactionTimeRangeAveragesJpaRepository extends JpaRepository<MarketTransactionTimeRangeAveragesEntity, UUID> {
    @Query("SELECT m FROM MarketTransactionTimeRangeAveragesEntity m WHERE m.resource = :resource ORDER BY m.toDate DESC")
    List<MarketTransactionTimeRangeAveragesEntity> findTopByResourceOrderByToDateDesc(@Param("resource") MarketResource resource, Pageable pageable);
}
