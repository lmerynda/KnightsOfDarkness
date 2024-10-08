package com.knightsofdarkness.game.market;

import java.util.Optional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.knightsofdarkness.common.market.BuyMarketOfferResult;
import com.knightsofdarkness.common.market.CreateMarketOfferResult;
import com.knightsofdarkness.common.market.MarketResource;
import com.knightsofdarkness.common.market.RemoveMarketOfferResult;
import com.knightsofdarkness.game.kingdom.Kingdom;

public interface IMarket {

    CreateMarketOfferResult createOffer(String kingdomName, MarketResource resource, int count, int price);

    RemoveMarketOfferResult removeOffer(MarketOffer offer);

    RemoveMarketOfferResult removeOffer(UUID id);

    List<MarketOffer> getOffersByResource(MarketResource resource);

    Optional<MarketOffer> getCheapestOfferByResource(MarketResource resource);

    List<MarketOffer> getOffersByKingdomName(String name);

    Optional<MarketOffer> findOfferById(UUID id);

    BuyMarketOfferResult buyExistingOffer(MarketOffer offer, Kingdom seller, Kingdom buyer, int amount);

    void updateMarketTransactionsAverages(Instant from, Instant to);

    Optional<Double> getLast24TransactionAverages(MarketResource resource);

    List<MarketTransaction> getTransactionsByResourceAndTimeRange(MarketResource resource, Instant from, Instant to);
}
