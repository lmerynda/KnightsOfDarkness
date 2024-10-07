package com.knightsofdarkness.game.gameconfig;

import com.knightsofdarkness.common.market.MarketResource;

public record CarrierCapacity(int iron, int food, int tools, int weapons) {
    public int get(MarketResource resource)
    {
        switch (resource)
        {
            case iron:
                return iron;
            case food:
                return food;
            case tools:
                return tools;
            case weapons:
                return weapons;
            default:
                throw new IllegalArgumentException("Unknown market resource: " + resource);
        }
    }
}
