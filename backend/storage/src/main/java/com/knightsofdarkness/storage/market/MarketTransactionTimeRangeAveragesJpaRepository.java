package com.knightsofdarkness.storage.market;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketTransactionTimeRangeAveragesJpaRepository extends JpaRepository<MarketTransactionTimeRangeAveragesEntity, UUID> {

}
