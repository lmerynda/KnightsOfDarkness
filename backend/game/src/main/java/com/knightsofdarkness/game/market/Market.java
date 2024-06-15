package com.knightsofdarkness.game.market;

import java.util.Optional;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knightsofdarkness.game.Id;
import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.game.storage.IMarketOfferRepository;

public class Market implements IMarket {
    private static final Logger log = LoggerFactory.getLogger(Market.class);
    IMarketOfferRepository offers;

    public Market(IMarketOfferRepository repository)
    {
        this.offers = repository;
    }

    @Override
    public MarketOffer addOffer(Kingdom kingdom, MarketResource resource, int count, int price)
    {
        var offer = new MarketOffer(Id.generate(), kingdom, resource, count, price);
        offers.add(offer);

        return offer;
    }

    @Override
    public void removeOffer(MarketOffer offer)
    {
        offers.remove(offer);
    }

    @Override
    public List<MarketOffer> getOffersByResource(MarketResource resource)
    {
        return offers.getOffersByResource(resource);
    }

    @Override
    public Optional<MarketOffer> getCheapestOfferByResource(MarketResource resource)
    {
        return offers.getCheapestOfferByResource(resource);
    }

    @Override
    public List<MarketOffer> getOffersByKingdom(Kingdom kingdom)
    {
        return offers.getOffersByKingdomName(kingdom.getName());
    }

    public List<MarketOffer> getOffersByKingdomName(String name)
    {
        return offers.getOffersByKingdomName(name);
    }

    @Override
    public Optional<MarketOffer> findOfferById(UUID id)
    {
        return offers.findById(id);
    }

    // TODO make a LOT of tests for this
    /**
     * @return amount of resource which was actually sold
     */
    @Override
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
            // offers.remove(sellingOffer);
            log.info("Should normally remove offer because it's empty");
        }

        return maxToSell;
    }

    @Override
    public void update(MarketOffer offer)
    {
        offers.update(offer);
    }

    static int offerComparator(MarketOffer offer1, MarketOffer offer2)
    {
        return offer1.price - offer2.price;
    }
}
