package com.knightsofdarkness.game.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.knightsofdarkness.game.market.MarketOffer;
import com.knightsofdarkness.game.market.MarketResource;
import com.knightsofdarkness.game.storage.IMarketOfferRepository;

public class MarketRepository implements IMarketOfferRepository {
    List<MarketOffer> offers = new ArrayList<>();

    @Override
    public MarketOffer add(MarketOffer marketOffer)
    {
        offers.add(marketOffer);
        return marketOffer;
    }

    @Override
    public void remove(MarketOffer marketOffer)
    {
        offers.remove(marketOffer);
    }

    @Override
    public List<MarketOffer> getOffersByResource(MarketResource resource)
    {
        return offers.stream().filter(offer -> offer.getResource().equals(resource)).toList();
    }

    @Override
    public Optional<MarketOffer> getCheapestOfferByResource(MarketResource resource)
    {
        return offers.stream()
                .filter(offer -> offer.getResource().equals(resource))
                .sorted((left, right) -> Integer.compare(left.getPrice(), right.getPrice()))
                .findFirst();
    }

    @Override
    public List<MarketOffer> getOffersByKingdomId(Long kingdomId)
    {
        // TODO use kingdom repository to get kingdom by id and then get offers by kingdom
        return offers.stream().filter(offer -> offer.getKingdom().getId().equals(kingdomId)).toList();
    }

    @Override
    public Optional<MarketOffer> findById(long marketOfferId)
    {
        return offers.stream().filter(offer -> offer.getId() == marketOfferId).findFirst();
    }
}
