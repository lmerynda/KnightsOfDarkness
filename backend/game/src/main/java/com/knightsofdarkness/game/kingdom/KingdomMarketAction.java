package com.knightsofdarkness.game.kingdom;

import com.knightsofdarkness.game.market.MarketOffer;
import com.knightsofdarkness.game.market.MarketResource;

public class KingdomMarketAction {
    private final Kingdom kingdom;

    public KingdomMarketAction(Kingdom kingdom)
    {
        this.kingdom = kingdom;
    }

    public void postOffer(MarketResource resource, int count, int price)
    {
        // TODO in game configuration add limit on number of offers from a single
        // kingdom
        var maxToPost = Math.min(count, kingdom.getResources().getCount(ResourceName.from(resource)));
        if (maxToPost > 0)
        {
            kingdom.getMarket().addOffer(kingdom, resource, count, price);
            kingdom.getResources().subtractCount(ResourceName.from(resource), count);
        }
        // TODO test above scenarios
    }

    public int buyMarketOffer(MarketOffer offer, int amount)
    {
        // TODO test scenario for both sides of each Math.min()
        var gold = kingdom.getResources().getCount(ResourceName.gold);
        var maxToSpent = Math.min(gold, amount * offer.getPrice());
        var maxAmount = Math.min(maxToSpent / offer.getPrice(), amount);
        var amountBought = kingdom.getMarket().buyExistingOffer(offer, maxAmount);
        kingdom.getResources().subtractCount(ResourceName.gold, amountBought * offer.getPrice());
        kingdom.getResources().addCount(ResourceName.from(offer.getResource()), amountBought);

        return amountBought;
    }

    public void acceptOffer(int goldValue)
    {
        // TODO enforced a tax on transactions to make changes visible with current crude test models, remove!
        kingdom.getResources().addCount(ResourceName.gold, (int) (goldValue * 0.9));
    }

    public void withdrawMarketOffer(MarketOffer offer)
    {
        if (offer.getKingdom() == kingdom)
        {
            kingdom.getMarket().removeOffer(offer);
            kingdom.getResources().addCount(ResourceName.from(offer.getResource()), offer.getCount());
        }
    }
}
