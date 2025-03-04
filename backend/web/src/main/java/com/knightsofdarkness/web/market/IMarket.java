package com.knightsofdarkness.web.market;

import java.util.Optional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.knightsofdarkness.common.market.BuyMarketOfferResult;
import com.knightsofdarkness.common.market.CreateMarketOfferResult;
import com.knightsofdarkness.common.market.MarketResource;
import com.knightsofdarkness.common.market.RemoveMarketOfferResult;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;
import com.knightsofdarkness.web.market.model.MarketOfferEntity;
import com.knightsofdarkness.web.market.model.MarketTransactionEntity;

public interface IMarket {

    CreateMarketOfferResult createOffer(String kingdomName, MarketResource resource, int count, int price);

    RemoveMarketOfferResult removeOffer(MarketOfferEntity offer);

    RemoveMarketOfferResult removeOffer(UUID id);

    List<MarketOfferEntity> getOffersByResource(MarketResource resource);

    Optional<MarketOfferEntity> getCheapestOfferByResource(MarketResource resource);

    List<MarketOfferEntity> getOffersByKingdomName(String name);

    Optional<MarketOfferEntity> findOfferById(UUID id);

    BuyMarketOfferResult buyExistingOffer(MarketOfferEntity offer, KingdomEntity seller, KingdomEntity buyer, int amount);

    void updateMarketTransactionsAverages(Instant from, Instant to);

    Optional<Double> getLast24TransactionAverages(MarketResource resource);

    List<MarketTransactionEntity> getTransactionsByResourceAndTimeRange(MarketResource resource, Instant from, Instant to);
}
