package com.knightsofdarkness.game.market;

import java.util.Optional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.knightsofdarkness.game.kingdom.Kingdom;

public interface IMarket {

    MarketOffer addOffer(Kingdom kingdom, MarketResource resource, int count, int price);

    void removeOffer(MarketOffer offer);

    List<MarketOffer> getOffersByResource(MarketResource resource);

    Optional<MarketOffer> getCheapestOfferByResource(MarketResource resource);

    List<MarketOffer> getOffersByKingdomName(String name);

    Optional<MarketOffer> findOfferById(UUID id);

    MarketOfferBuyResult buyExistingOffer(MarketOffer offer, Kingdom seller, Kingdom buyer, int amount);

    void updateMarketTransactionsAverages(Instant from, Instant to);

    Optional<Double> getLast24TransactionAverages(MarketResource resource);

    List<MarketTransaction> getTransactionsByResourceAndTimeRange(MarketResource resource, Instant from, Instant to);
}
