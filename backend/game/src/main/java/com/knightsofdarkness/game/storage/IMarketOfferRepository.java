package com.knightsofdarkness.game.storage;

import java.util.Optional;

import java.util.List;
import java.util.UUID;

import com.knightsofdarkness.game.market.MarketOffer;
import com.knightsofdarkness.game.market.MarketResource;
import com.knightsofdarkness.game.market.MarketTransaction;

public interface IMarketOfferRepository {
    MarketOffer add(MarketOffer marketOffer);

    void remove(MarketOffer marketOffer);

    List<MarketOffer> getOffersByResource(MarketResource resource);

    Optional<MarketOffer> getCheapestOfferByResource(MarketResource resource);

    List<MarketOffer> getOffersByKingdomName(String name);

    Optional<MarketOffer> findById(UUID marketOfferId);

    void update(MarketOffer marketOffer);

    void registerMarketTransaction(MarketTransaction transaction);
}
