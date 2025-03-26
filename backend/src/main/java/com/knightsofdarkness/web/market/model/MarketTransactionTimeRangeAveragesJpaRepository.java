package com.knightsofdarkness.web.market.model;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.knightsofdarkness.web.common.market.MarketResource;

@Repository
public interface MarketTransactionTimeRangeAveragesJpaRepository extends JpaRepository<MarketTransactionTimeRangeAveragesEntity, UUID> {
    @Query("SELECT m FROM MarketTransactionTimeRangeAveragesEntity m WHERE m.resource = :resource ORDER BY m.toDate DESC")
    List<MarketTransactionTimeRangeAveragesEntity> findTopByResourceOrderByToDateDesc(@Param("resource") MarketResource resource, Pageable pageable);
}
