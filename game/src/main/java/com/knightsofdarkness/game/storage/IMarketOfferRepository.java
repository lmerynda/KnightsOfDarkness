package com.knightsofdarkness.game.storage;

import java.util.List;

import com.knightsofdarkness.game.market.MarketOffer;

public interface IMarketOfferRepository {
    MarketOffer save(MarketOffer marketOffer);

    void deleteById(long marketOfferId);

    MarketOffer findById(long marketOfferId);

    List<MarketOffer> findAll();
}
