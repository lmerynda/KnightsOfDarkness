package com.knightsofdarkness.web.kingdom.model;

import com.knightsofdarkness.common.kingdom.ResourceName;
import com.knightsofdarkness.common.market.MarketResource;
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
            kingdom.getResources().subtractCount(ResourceName.from(resource), count);
        }

        return maxToPost;
    }

    public void acceptOffer(KingdomEntity seller, int goldValue)
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

    public int reserveGoldForOffer(int price, int amount)
    {
        var gold = kingdom.getResources().getCount(ResourceName.gold);
        var maxToSpend = Math.min(gold, amount * price);
        var amoutTobuy = maxToSpend / price; // notice the truncation, it is important as should only pay for what can be bought
        maxToSpend = amoutTobuy * price;
        kingdom.getResources().subtractCount(ResourceName.gold, maxToSpend);

        return maxToSpend;
    }

    public void deliverResourcesFromOffer(MarketResource resource, int amount)
    {
        kingdom.getResources().addCount(ResourceName.from(resource), amount);
    }
}
