package com.knightsofdarkness.common.market;

import java.util.UUID;

public record OfferDto(UUID id, String sellerName, String resource, int count, int price) {
}
