package com.knightsofdarkness.web.market.model;

import java.util.Optional;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.knightsofdarkness.common.market.MarketResource;

@Repository
public interface MarketOfferJpaRepository extends JpaRepository<MarketOfferEntity, UUID> {
    List<MarketOfferEntity> findByResource(MarketResource resource);

    List<MarketOfferEntity> findByKingdomName(String name);

    Optional<MarketOfferEntity> findFirstByResourceOrderByPriceAsc(MarketResource resource);

    List<MarketOfferEntity> findByResourceOrderByPriceAsc(MarketResource resource, Pageable pageable);

    int countByKingdomNameAndResource(String kingdomName, MarketResource resource);
}
