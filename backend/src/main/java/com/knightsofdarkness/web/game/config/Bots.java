package com.knightsofdarkness.web.game.config;

import java.util.Map;

import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.market.MarketResource;

/**
 * Merchant bots play every turn the moment it arrives, so without limits they outgrow every player, flood the market and
 * hoard stores worth plundering. They stop buying land at maxLand, offer at most maxOffer of a good at a time, and once
 * all bots have acted they ship anything above maxStock to distant markets (player purchases until their next run can
 * lift them above it again).
 */
public record Bots(int maxLand, Map<ResourceName, Integer> maxStock, Map<MarketResource, Integer> maxOffer) {
    public int getMaxStock(ResourceName resource)
    {
        return maxStock.getOrDefault(resource, Integer.MAX_VALUE);
    }

    public int getMaxOffer(MarketResource resource)
    {
        return maxOffer.getOrDefault(resource, Integer.MAX_VALUE);
    }
}
