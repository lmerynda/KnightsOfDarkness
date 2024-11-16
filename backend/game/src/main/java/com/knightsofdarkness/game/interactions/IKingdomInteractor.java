package com.knightsofdarkness.game.interactions;

import com.knightsofdarkness.common.market.MarketResource;
import com.knightsofdarkness.game.kingdom.Kingdom;

public interface IKingdomInteractor {
    void transferResources(Kingdom from, String to, MarketResource resource, int amount);
}