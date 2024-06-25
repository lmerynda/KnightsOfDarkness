package com.knightsofdarkness.storage.market;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.knightsofdarkness.common.MarketOfferDto;
import com.knightsofdarkness.game.market.MarketResource;

@Repository
public class MarketOfferReadRepository {
    private final MarketOfferJpaRepository jpaRepository;

    public MarketOfferReadRepository(MarketOfferJpaRepository marketOfferJpaRepository) {
        this.jpaRepository = marketOfferJpaRepository;
    }

    public List<MarketOfferDto> getOffersByResource(MarketResource resource)
    {
        var offers = jpaRepository.findByResource(resource);
        return offers.stream().map(MarketOfferEntity::toDto).toList();
    }

    public Optional<MarketOfferDto> findOfferById(UUID id)
    {
        return jpaRepository.findById(id).map(MarketOfferEntity::toDto);
    }
}
