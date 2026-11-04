package com.knightsofdarkness.web.common.kingdom;

import java.util.Objects;

import com.knightsofdarkness.web.common.market.MarketResource;

public record SendCarriersDto(String destinationKingdomName, MarketResource resource, int count) {
    /** a request without these is refused as malformed, before it reaches the game */
    public SendCarriersDto
    {
        Objects.requireNonNull(destinationKingdomName, "destinationKingdomName");
        Objects.requireNonNull(resource, "resource");
    }
}
