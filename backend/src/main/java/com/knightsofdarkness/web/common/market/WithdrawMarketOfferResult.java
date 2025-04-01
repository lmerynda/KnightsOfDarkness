package com.knightsofdarkness.web.common.market;

public record WithdrawMarketOfferResult(String message, boolean success) {
    public static WithdrawMarketOfferResult success(String message)
    {
        return new WithdrawMarketOfferResult(message, true);
    }

    public static WithdrawMarketOfferResult failure(String message)
    {
        return new WithdrawMarketOfferResult(message, false);
    }
}
