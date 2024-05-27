package com.knightsofdarkness.game.market;

import java.util.List;
import java.util.Optional;

import com.knightsofdarkness.game.kingdom.Kingdom;

public interface IMarket {

    MarketOffer addOffer(Kingdom kingdom, MarketResource resource, int count, int price);

    void removeOffer(MarketOffer offer);

    List<MarketOffer> getOffersByResource(MarketResource resource);

    Optional<MarketOffer> getCheapestOfferByResource(MarketResource resource);

    List<MarketOffer> getOffersByKingdom(Kingdom kingdom);

    /**
     * @return amount of resource which was actually sold
     */
    int buyExistingOffer(MarketOffer offer, int amount);

}