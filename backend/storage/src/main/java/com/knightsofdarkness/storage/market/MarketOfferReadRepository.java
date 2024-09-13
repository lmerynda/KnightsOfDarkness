package com.knightsofdarkness.storage.market;

import java.util.Optional;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.knightsofdarkness.common.market.MarketOfferDto;
import com.knightsofdarkness.common.market.MarketResource;

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

    public List<MarketOfferDto> findCheapestOffersByResource(MarketResource resource, int limit)
    {
        Pageable pageable = PageRequest.of(0, limit);
        var offers = jpaRepository.findByResourceOrderByPriceAsc(resource, pageable);
        return offers.stream().map(MarketOfferEntity::toDto).toList();
    }

    public Optional<MarketOfferDto> findOfferById(UUID id)
    {
        return jpaRepository.findById(id).map(MarketOfferEntity::toDto);
    }

    public List<MarketOfferDto> findByKingdomName(String name)
    {
        return jpaRepository.findByKingdomName(name).stream().map(MarketOfferEntity::toDto).toList();
    }
}
