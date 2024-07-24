package com.knightsofdarkness.game.market;

import java.util.Optional;

import java.util.List;
import java.util.UUID;

import com.knightsofdarkness.game.kingdom.Kingdom;

public interface IMarket {

    MarketOffer addOffer(Kingdom kingdom, MarketResource resource, int count, int price);

    void removeOffer(MarketOffer offer);

    List<MarketOffer> getOffersByResource(MarketResource resource);

    Optional<MarketOffer> getCheapestOfferByResource(MarketResource resource);

    @Deprecated
    List<MarketOffer> getOffersByKingdom(Kingdom kingdom);

    List<MarketOffer> getOffersByKingdomName(String name);

    Optional<MarketOffer> findOfferById(UUID id);

    MarketOfferBuyResult buyExistingOffer(MarketOffer offer, Kingdom seller, Kingdom buyer, int amount);
}