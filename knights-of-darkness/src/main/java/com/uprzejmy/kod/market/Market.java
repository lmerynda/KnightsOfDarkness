package com.uprzejmy.kod.market;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.uprzejmy.kod.kingdom.Kingdom;

public class Market
{
    List<MarketOffer> offers = new ArrayList<>();

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
        return offers.stream().filter(offer -> offer.resource == resource).toList();
    }

    public Optional<MarketOffer> getCheapestOfferByResource(MarketResource resource)
    {
        return offers.stream().filter(offer -> offer.resource == resource).sorted(Market::offerComparator).findFirst();
    }

    public List<MarketOffer> getOffersByKingdom(Kingdom kingdom)
    {
        return offers.stream().filter(offer -> offer.kingdom == kingdom).toList();
    }

    // TODO make a LOT of tests for this
    /**
     * @return amount of resource which was actually sold
     */
    public int buyExistingOffer(MarketOffer offer, int amount)
    {
        var potentialOffer = offers.stream().filter(element -> element.equals(offer)).findFirst();
        if (!potentialOffer.isPresent())
        {
            return 0;
        }

        var sellingOffer = potentialOffer.get();

        var maxToSell = Math.min(sellingOffer.count, amount);
        sellingOffer.count -= maxToSell;
        var goldDue = maxToSell * sellingOffer.getPrice();
        offer.kingdom.acceptMarketOffer(goldDue);

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
