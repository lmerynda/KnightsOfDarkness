package com.knightsofdarkness.web.common.market;

import java.util.Objects;
import java.util.UUID;

public record MarketOfferDto(UUID id, String sellerName, MarketResource resource, int count, int price) {
    /** a request without these is refused as malformed, before it reaches the game */
    public MarketOfferDto
    {
        Objects.requireNonNull(resource, "resource");
    }
}
