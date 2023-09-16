package com.uprzejmy.kod.kingdom;

import java.util.List;

import com.uprzejmy.kod.market.MarketRecord;
import com.uprzejmy.kod.market.MarketResource;

public class KingdomMarketAction
{
    private final Kingdom kingdom;

    public KingdomMarketAction(Kingdom kingdom)
    {
        this.kingdom = kingdom;
    }

    public void postOffer(MarketResource resource, int count, int price)
    {
        // TODO validate if there are enough resources in kingdom before posting the offer
        // TODO remove resources from kingdom when offer has been accepted
        // TODO in game configuration add limit on number of offers from a single kingdom
        // REMINDER test above scenarios
        kingdom.getMarket().addOffer(kingdom, resource, count, price);
    }

    public List<MarketRecord> getMyOffers()
    {
        return kingdom.getMarket().getOffersByKingdom(kingdom);
    }
}
