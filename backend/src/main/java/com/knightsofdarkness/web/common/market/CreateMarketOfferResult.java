package com.knightsofdarkness.web.common.market;

import java.util.Optional;

public record CreateMarketOfferResult(String message, boolean success, Optional<MarketOfferDto> data) {
    public static CreateMarketOfferResult success(String message, MarketOfferDto offer)
    {
        return new CreateMarketOfferResult(message, true, Optional.of(offer));
    }

    public static CreateMarketOfferResult failure(String message, Optional<MarketOfferDto> data)
    {
        return new CreateMarketOfferResult(message, false, data);
    }
}
