package com.knightsofdarkness.web.kingdom.model;

import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.market.MarketResource;
import com.knightsofdarkness.web.market.model.MarketOfferEntity;

public class KingdomMarketAction {
    private final KingdomEntity kingdom;

    public KingdomMarketAction(KingdomEntity kingdom)
    {
        this.kingdom = kingdom;
    }

    public int postOffer(MarketResource resource, int count)
    {
        var maxToPost = Math.min(count, kingdom.getResources().getCount(ResourceName.from(resource)));
        if (maxToPost > 0)
        {
            kingdom.getResources().subtractCount(ResourceName.from(resource), maxToPost);
        }

        return maxToPost;
    }

    public void acceptOffer(int goldValue)
    {
        kingdom.getResources().addCount(ResourceName.gold, goldValue);
    }

    public void withdrawMarketOffer(MarketOfferEntity offer)
    {
        if (offer.getSeller().getName().equals(kingdom.getName()))
        {
            kingdom.getResources().addCount(ResourceName.from(offer.getResource()), offer.getCount());
        }
    }

    public int reserveGoldForOffer(KingdomEntity buyer, int price, int count)
    {
        var gold = buyer.getResources().getCount(ResourceName.gold);
        var maxToSpend = Math.min(gold, count * price);
        var amoutTobuy = maxToSpend / price; // notice the truncation, it is important as should only pay for what can be bought
        maxToSpend = amoutTobuy * price;
        buyer.getResources().subtractCount(ResourceName.gold, maxToSpend);

        return maxToSpend;
    }

    public void deliverResourcesFromOffer(KingdomEntity buyer, MarketResource resource, int count)
    {
        buyer.getResources().addCount(ResourceName.from(resource), count);
    }
}
