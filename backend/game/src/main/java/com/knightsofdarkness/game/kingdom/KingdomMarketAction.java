package com.knightsofdarkness.game.kingdom;

import com.knightsofdarkness.game.market.MarketOffer;
import com.knightsofdarkness.game.market.MarketResource;

public class KingdomMarketAction {
    private final Kingdom kingdom;

    public KingdomMarketAction(Kingdom kingdom)
    {
        this.kingdom = kingdom;
    }

    public int postOffer(MarketResource resource, int count)
    {
        // TODO in game configuration add limit on number of offers from a single kingdom
        var maxToPost = Math.min(count, kingdom.getResources().getCount(ResourceName.from(resource)));
        if (maxToPost > 0)
        {
            kingdom.getResources().subtractCount(ResourceName.from(resource), count);
        }

        return maxToPost;
        // TODO test above scenarios
    }

    public void acceptOffer(int goldValue)
    {
        kingdom.getResources().addCount(ResourceName.gold, goldValue);
    }

    public void withdrawMarketOffer(MarketOffer offer)
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
        kingdom.getResources().subtractCount(ResourceName.gold, maxToSpend);

        return maxToSpend;
    }

    public void deliverResourcesFromOffer(MarketResource resource, int amount)
    {
        kingdom.getResources().addCount(ResourceName.from(resource), amount);
    }
}
