package com.knightsofdarkness.web.market.model;

import java.util.Optional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.knightsofdarkness.common.market.MarketResource;

@Repository
public class MarketOfferRepository implements IMarketOfferRepository {
    private final MarketOfferJpaRepository jpaRepository;
    private final MarketTransactionJpaRepository transactionJpaRepository;
    private final MarketTransactionTimeRangeAveragesJpaRepository transactionAveragesJpaRepository;

    public MarketOfferRepository(MarketOfferJpaRepository jpaRepository, MarketTransactionJpaRepository transactionJpaRepository, MarketTransactionTimeRangeAveragesJpaRepository transactionAveragesJpaRepository)
    {
        this.jpaRepository = jpaRepository;
        this.transactionJpaRepository = transactionJpaRepository;
        this.transactionAveragesJpaRepository = transactionAveragesJpaRepository;
    }

    @Override
    public MarketOfferEntity add(MarketOfferEntity marketOffer)
    {
        return jpaRepository.save(marketOffer);
    }

    @Override
    public void remove(MarketOfferEntity marketOffer)
    {
        jpaRepository.delete(marketOffer);
    }

    @Override
    public List<MarketOfferEntity> getOffersByResource(MarketResource resource)
    {
        return jpaRepository.findByResource(resource);
    }

    @Override
    public Optional<MarketOfferEntity> getCheapestOfferByResource(MarketResource resource)
    {
        return jpaRepository.findFirstByResourceOrderByPriceAsc(resource);
    }

    @Override
    public List<MarketOfferEntity> getOffersByKingdomName(String name)
    {
        return jpaRepository.findByKingdomName(name);
    }

    @Override
    public Optional<MarketOfferEntity> findById(UUID marketOfferId)
    {
        return jpaRepository.findById(marketOfferId);
    }

    public void update(MarketOfferEntity marketOffer)
    {
        jpaRepository.save(marketOffer);
    }

    @Override
    public void registerMarketTransaction(MarketTransactionEntity transaction)
    {
        transactionJpaRepository.save(transaction);
    }

    @Override
    public List<MarketTransactionEntity> getTransactionsByResourceAndTimeRange(MarketResource resource, Instant hourAgo, Instant now)
    {
        return transactionJpaRepository.findTransactionsForResourceAndTimeRange(resource, hourAgo, now);
    }

    @Override
    public void addTransactionTimeRangeAverage(MarketTransactionTimeRangeAveragesEntity averageSaleRecord)
    {
        transactionAveragesJpaRepository.save(averageSaleRecord);
    }

    @Override
    public List<MarketTransactionTimeRangeAveragesEntity> getTransactionTimeRangeAverages(MarketResource resource, int limit)
    {
        PageRequest pageable = PageRequest.of(0, limit);
        return transactionAveragesJpaRepository.findTopByResourceOrderByToDateDesc(resource, pageable);
    }

    @Override
    public int getOffersCountByKingdomNameAndResource(String kingdomName, MarketResource resource)
    {
        return jpaRepository.countByKingdomNameAndResource(kingdomName, resource);
    }
}
