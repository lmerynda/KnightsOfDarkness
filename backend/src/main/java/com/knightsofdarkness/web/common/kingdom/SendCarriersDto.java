package com.knightsofdarkness.web.common.kingdom;

import com.knightsofdarkness.web.common.market.MarketResource;

public record SendCarriersDto(String destinationKingdomName, MarketResource resource, int amount) {

}
