package com.knightsofdarkness.game.storage;

import java.util.Optional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.knightsofdarkness.common.market.MarketResource;
import com.knightsofdarkness.game.market.MarketOffer;
import com.knightsofdarkness.game.market.MarketTransaction;
import com.knightsofdarkness.game.market.MarketTransactionTimeRangeAverage;

public interface IMarketOfferRepository {
    MarketOffer add(MarketOffer marketOffer);

    void remove(MarketOffer marketOffer);

    List<MarketOffer> getOffersByResource(MarketResource resource);

    Optional<MarketOffer> getCheapestOfferByResource(MarketResource resource);

    List<MarketOffer> getOffersByKingdomName(String name);

    int getOffersCountByKingdomNameAndResource(String kingdomName, MarketResource resource);

    Optional<MarketOffer> findById(UUID marketOfferId);

    void update(MarketOffer marketOffer);

    void registerMarketTransaction(MarketTransaction transaction);

    List<MarketTransaction> getTransactionsByResourceAndTimeRange(MarketResource resource, Instant hourAgo, Instant now);

    void addTransactionTimeRangeAverage(MarketTransactionTimeRangeAverage averageSaleRecord);

    List<MarketTransactionTimeRangeAverage> getTransactionTimeRangeAverages(MarketResource resource, int limit);
}
