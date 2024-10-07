package com.knightsofdarkness.common.kingdom;

import com.knightsofdarkness.common.market.MarketResource;

public record SendCarriersDto(String destinationKingdomName, MarketResource resource, int amount) {

}
