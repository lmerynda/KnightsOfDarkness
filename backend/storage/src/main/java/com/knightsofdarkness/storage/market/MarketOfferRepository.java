package com.knightsofdarkness.storage.market;

import java.util.Optional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.knightsofdarkness.common.market.MarketResource;
import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.game.market.MarketOffer;
import com.knightsofdarkness.game.market.MarketTransaction;
import com.knightsofdarkness.game.market.MarketTransactionTimeRangeAverage;
import com.knightsofdarkness.game.storage.IMarketOfferRepository;

@Repository
public class MarketOfferRepository implements IMarketOfferRepository {
    private final GameConfig gameConfig;
    private final MarketOfferJpaRepository jpaRepository;
    private final MarketTransactionJpaRepository transactionJpaRepository;
    private final MarketTransactionTimeRangeAveragesJpaRepository transactionAveragesJpaRepository;

    public MarketOfferRepository(GameConfig gameConfig, MarketOfferJpaRepository jpaRepository, MarketTransactionJpaRepository transactionJpaRepository, MarketTransactionTimeRangeAveragesJpaRepository transactionAveragesJpaRepository)
    {
        this.gameConfig = gameConfig;
        this.jpaRepository = jpaRepository;
        this.transactionJpaRepository = transactionJpaRepository;
        this.transactionAveragesJpaRepository = transactionAveragesJpaRepository;
    }

    @Override
    public MarketOffer add(MarketOffer marketOffer)
    {
        var marketOfferEntity = jpaRepository.save(MarketOfferEntity.fromDomainModel(marketOffer));

        return marketOfferEntity.toDomainModel(gameConfig);
    }

    @Override
    public void remove(MarketOffer marketOffer)
    {
        var marketOfferEntity = MarketOfferEntity.fromDomainModel(marketOffer);
        jpaRepository.delete(marketOfferEntity);
    }

    @Override
    public List<MarketOffer> getOffersByResource(MarketResource resource)
    {
        var offers = jpaRepository.findByResource(resource);
        return offers.stream().map(marketOfferEntity -> marketOfferEntity.toDomainModel(gameConfig)).toList();
    }

    @Override
    public Optional<MarketOffer> getCheapestOfferByResource(MarketResource resource)
    {
        return jpaRepository.findFirstByResourceOrderByPriceAsc(resource).map(marketOfferEntity -> marketOfferEntity.toDomainModel(gameConfig));
    }

    @Override
    public List<MarketOffer> getOffersByKingdomName(String name)
    {
        return jpaRepository.findByKingdomName(name).stream().map(marketOfferEntity -> marketOfferEntity.toDomainModel(gameConfig)).toList();
    }

    @Override
    public Optional<MarketOffer> findById(UUID marketOfferId)
    {
        var marketOffer = jpaRepository.findById(marketOfferId);
        return marketOffer.map(marketOfferEntity -> marketOfferEntity.toDomainModel(gameConfig));
    }

    public void update(MarketOffer marketOffer)
    {
        var marketOfferEntity = MarketOfferEntity.fromDomainModel(marketOffer);
        jpaRepository.save(marketOfferEntity);
    }

    @Override
    public void registerMarketTransaction(MarketTransaction transaction)
    {
        var transactionEntity = MarketTransactionEntity.fromDomainModel(transaction);
        transactionJpaRepository.save(transactionEntity);
    }

    @Override
    public List<MarketTransaction> getTransactionsByResourceAndTimeRange(MarketResource resource, Instant hourAgo, Instant now)
    {
        return transactionJpaRepository.findTransactionsForResourceAndTimeRange(resource, hourAgo, now).stream().map(MarketTransactionEntity::toDomainModel).toList();
    }

    @Override
    public void addTransactionTimeRangeAverage(MarketTransactionTimeRangeAverage averageSaleRecord)
    {
        var averageSaleRecordEntity = MarketTransactionTimeRangeAveragesEntity.fromDomainModel(averageSaleRecord);
        transactionAveragesJpaRepository.save(averageSaleRecordEntity);
    }

    @Override
    public List<MarketTransactionTimeRangeAverage> getTransactionTimeRangeAverages(MarketResource resource, int limit)
    {
        PageRequest pageable = PageRequest.of(0, limit);
        return transactionAveragesJpaRepository.findTopByResourceOrderByToDateDesc(resource, pageable).stream().map(MarketTransactionTimeRangeAveragesEntity::toDomainModel).toList();
    }

    @Override
    public int getOffersCountByKingdomNameAndResource(String kingdomName, MarketResource resource)
    {
        return jpaRepository.countByKingdomNameAndResource(kingdomName, resource);
    }
}
