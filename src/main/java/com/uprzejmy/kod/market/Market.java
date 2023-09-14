package com.uprzejmy.kod.market;

import java.util.ArrayList;
import java.util.List;

import com.uprzejmy.kod.kingdom.Kingdom;
import com.uprzejmy.kod.kingdom.MarketResource;

public class Market
{
    List<MarketRecord> offers = new ArrayList<>();

    MarketRecord addOffer(Kingdom kingdom, MarketResource resource, int count, int price)
    {
        var offer = new MarketRecord(kingdom, resource, count, price);
        offers.add(offer);

        return offer;
    }

    void removeOffer(MarketRecord offer)
    {
        offers.remove(offer);
    }

    List<MarketRecord> getOffersByResource(MarketResource resource)
    {
        return offers.stream().filter(offer -> offer.resource == resource).toList();
    }

    List<MarketRecord> getOffersByKingdom(Kingdom kingdom)
    {
        return offers.stream().filter(offer -> offer.kingdom == kingdom).toList();
    }
}
