package com.knightsofdarkness.game.market;

import java.util.List;
import java.util.Optional;

import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.game.storage.IMarketOfferRepository;

public class Market {
    IMarketOfferRepository offers;

    public Market(IMarketOfferRepository repository)
    {
        this.offers = repository;
    }

    public MarketOffer addOffer(Kingdom kingdom, MarketResource resource, int count, int price)
    {
        var offer = new MarketOffer(kingdom, resource, count, price);
        offers.add(offer);

        return offer;
    }

    public void removeOffer(MarketOffer offer)
    {
        offers.remove(offer);
    }

    public List<MarketOffer> getOffersByResource(MarketResource resource)
    {
        return offers.getOffersByResource(resource);
    }

    public Optional<MarketOffer> getCheapestOfferByResource(MarketResource resource)
    {
        return offers.getCheapestOfferByResource(resource);
    }

    public List<MarketOffer> getOffersByKingdom(Kingdom kingdom)
    {
        return offers.getOffersByKingdomId(kingdom.getId());
    }

    // TODO make a LOT of tests for this
    /**
     * @return amount of resource which was actually sold
     */
    public int buyExistingOffer(MarketOffer offer, int amount)
    {
        var optional = offers.findById(offer.getId());
        if (optional.isEmpty())
        {
            return 0;
        }

        var sellingOffer = optional.get();
        var maxToSell = Math.min(sellingOffer.count, amount);
        sellingOffer.count -= maxToSell;
        var goldDue = maxToSell * sellingOffer.getPrice();
        sellingOffer.kingdom.acceptMarketOffer(goldDue);

        if (sellingOffer.count <= 0)
        {
            offers.remove(sellingOffer);
        }

        return maxToSell;
    }

    static int offerComparator(MarketOffer offer1, MarketOffer offer2)
    {
        return offer1.price - offer2.price;
    }
}
