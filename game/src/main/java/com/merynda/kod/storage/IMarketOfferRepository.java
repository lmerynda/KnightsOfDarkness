package com.merynda.kod.storage;

import java.util.List;

import com.merynda.kod.market.MarketOffer;

public interface IMarketOfferRepository {
    MarketOffer save(MarketOffer marketOffer);

    void deleteById(long marketOfferId);

    MarketOffer findById(long marketOfferId);

    List<MarketOffer> findAll();
}
