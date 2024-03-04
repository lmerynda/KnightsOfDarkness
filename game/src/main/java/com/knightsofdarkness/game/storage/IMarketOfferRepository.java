package com.knightsofdarkness.game.storage;

import java.util.List;
import java.util.Optional;

import com.knightsofdarkness.game.market.MarketOffer;
import com.knightsofdarkness.game.market.MarketResource;

public interface IMarketOfferRepository {
    MarketOffer add(MarketOffer marketOffer);

    void remove(MarketOffer marketOffer);

    List<MarketOffer> getOffersByResource(MarketResource resource);

    Optional<MarketOffer> getCheapestOfferByResource(MarketResource resource);

    List<MarketOffer> getOffersByKingdomId(Long kingdomId);

    Optional<MarketOffer> findById(long marketOfferId);
}
