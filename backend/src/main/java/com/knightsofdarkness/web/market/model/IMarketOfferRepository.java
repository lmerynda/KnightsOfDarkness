package com.knightsofdarkness.web.market.model;

import java.util.Optional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.knightsofdarkness.web.common.market.MarketResource;

public interface IMarketOfferRepository {
    MarketOfferEntity add(MarketOfferEntity marketOffer);

    void remove(MarketOfferEntity marketOffer);

    List<MarketOfferEntity> getOffersByResource(MarketResource resource);

    Optional<MarketOfferEntity> getCheapestOfferByResource(MarketResource resource);

    List<MarketOfferEntity> getOffersByKingdomName(String name);

    int getOffersCountByKingdomNameAndResource(String kingdomName, MarketResource resource);

    Optional<MarketOfferEntity> findById(UUID marketOfferId);

    void update(MarketOfferEntity marketOffer);

    void registerMarketTransaction(MarketTransactionEntity transaction);

    List<MarketTransactionEntity> getTransactionsByResourceAndTimeRange(MarketResource resource, Instant hourAgo, Instant now);

    void addTransactionTimeRangeAverage(MarketTransactionTimeRangeAveragesEntity averageSaleRecord);

    List<MarketTransactionTimeRangeAveragesEntity> getTransactionTimeRangeAverages(MarketResource resource, int limit);
}
