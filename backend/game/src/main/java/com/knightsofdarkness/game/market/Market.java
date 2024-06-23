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
    IMarketOfferRepository offersRepository;

    public Market(IMarketOfferRepository repository)
    {
        this.offersRepository = repository;
    }

    @Override
    public MarketOffer addOffer(Kingdom kingdom, MarketResource resource, int count, int price)
    {
        var countToOffer = kingdom.postMarketOffer(resource, count);
        var offer = new MarketOffer(Id.generate(), kingdom, resource, countToOffer, price);
        offersRepository.add(offer);
        // TODO update kingdom?

        return offer;
    }

    @Override
    public void removeOffer(MarketOffer offer)
    {
        offersRepository.remove(offer);
    }

    @Override
    public List<MarketOffer> getOffersByResource(MarketResource resource)
    {
        return offersRepository.getOffersByResource(resource);
    }

    @Override
    public Optional<MarketOffer> getCheapestOfferByResource(MarketResource resource)
    {
        return offersRepository.getCheapestOfferByResource(resource);
    }

    @Override
    public List<MarketOffer> getOffersByKingdom(Kingdom kingdom)
    {
        return offersRepository.getOffersByKingdomName(kingdom.getName());
    }

    public List<MarketOffer> getOffersByKingdomName(String name)
    {
        return offersRepository.getOffersByKingdomName(name);
    }

    @Override
    public Optional<MarketOffer> findOfferById(UUID id)
    {
        return offersRepository.findById(id);
    }

    // TODO make a LOT of tests for this
    /**
     * @return amount of resource which was actually sold
     */
    @Override
    public int buyExistingOffer(MarketOffer offer, Kingdom buyer, int amount)
    {
        var maxToSell = Math.min(offer.count, amount);
        var buyerGold = buyer.reserveGoldForOffer(offer.price, maxToSell);
        var buyerAmount = buyerGold / offer.price;
        offer.count -= buyerAmount;
        offer.seller.acceptMarketOffer(buyerGold);
        offersRepository.update(offer);

        if (offer.count <= 0)
        {
            // offers.remove(sellingOffer);
            log.info("Should normally remove offer because it's empty");
        }

        return buyerAmount;
    }

    static int offerComparator(MarketOffer offer1, MarketOffer offer2)
    {
        return offer1.price - offer2.price;
    }
}
