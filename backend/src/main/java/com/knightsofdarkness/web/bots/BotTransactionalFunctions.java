package com.knightsofdarkness.web.bots;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.knightsofdarkness.web.common.market.MarketPurchaseDto;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;
import com.knightsofdarkness.web.market.IMarket;
import com.knightsofdarkness.web.market.model.MarketOfferEntity;

public class BotTransactionalFunctions
{
    private BotTransactionalFunctions()
    {
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public static MarketPurchaseDto buyOffer(IMarket market, int countToBuy, MarketOfferEntity offer, KingdomEntity buyer)
    {
        return market.buyExistingOffer(offer, offer.getSeller(), buyer, countToBuy);
    }
}
