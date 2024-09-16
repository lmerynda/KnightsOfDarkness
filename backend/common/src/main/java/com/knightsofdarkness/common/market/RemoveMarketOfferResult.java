package com.knightsofdarkness.common.market;

public record RemoveMarketOfferResult(String message, boolean success) {
    public static RemoveMarketOfferResult success(String message)
    {
        return new RemoveMarketOfferResult(message, true);
    }

    public static RemoveMarketOfferResult failure(String message)
    {
        return new RemoveMarketOfferResult(message, false);
    }
}
