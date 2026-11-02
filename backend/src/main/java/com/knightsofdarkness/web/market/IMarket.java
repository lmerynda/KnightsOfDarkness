package com.knightsofdarkness.web.market;

import com.knightsofdarkness.web.common.market.MarketOfferDto;
import java.util.Optional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.knightsofdarkness.web.common.market.MarketPurchaseDto;
import com.knightsofdarkness.web.common.ActionResult;
import com.knightsofdarkness.web.common.market.MarketResource;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;
import com.knightsofdarkness.web.market.model.MarketOfferEntity;
import com.knightsofdarkness.web.market.model.MarketTransactionEntity;

public interface IMarket {

    ActionResult<MarketOfferDto> createOffer(String kingdomName, MarketResource resource, int count, int price);

    ActionResult<Void> removeOffer(MarketOfferEntity offer);

    ActionResult<Void> removeOffer(UUID id);

    List<MarketOfferEntity> getOffersByResource(MarketResource resource);

    Optional<MarketOfferEntity> getCheapestOfferByResource(MarketResource resource);

    List<MarketOfferEntity> getOffersByKingdomName(String name);

    Optional<MarketOfferEntity> findOfferById(UUID id);

    MarketPurchaseDto buyExistingOffer(MarketOfferEntity offer, KingdomEntity seller, KingdomEntity buyer, int count);

    void updateMarketTransactionsAverages(Instant from, Instant to);

    Optional<Double> getLast24TransactionAverages(MarketResource resource);

    List<MarketTransactionEntity> getTransactionsByResourceAndTimeRange(MarketResource resource, Instant from, Instant to);
}
