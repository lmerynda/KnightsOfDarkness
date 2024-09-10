package com.knightsofdarkness.common.market;

import java.util.Optional;

public record CreateOfferResult(String message, boolean success, Optional<OfferDto> data) {
    public static CreateOfferResult success(String message, OfferDto offer)
    {
        return new CreateOfferResult(message, true, Optional.of(offer));
    }

    public static CreateOfferResult failure(String message, Optional<OfferDto> data)
    {
        return new CreateOfferResult(message, false, data);
    }
}
