package com.knightsofdarkness.common.market;

import java.util.UUID;

public record MarketOfferDto(UUID id, String sellerName, MarketResource resource, int count, int price) {
}
