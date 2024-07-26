package com.knightsofdarkness.storage.market;

import java.util.Optional;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.knightsofdarkness.game.market.MarketResource;

@Repository
public interface MarketOfferJpaRepository extends JpaRepository<MarketOfferEntity, UUID> {
    List<MarketOfferEntity> findByResource(MarketResource resource);

    List<MarketOfferEntity> findByKingdomName(String name);

    Optional<MarketOfferEntity> findFirstByResourceOrderByPriceAsc(MarketResource resource);
}
