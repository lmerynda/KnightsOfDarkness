package com.knightsofdarkness.web.common.market;

public record MarketPurchaseDto(MarketResource resource, int count, int pricePerUnit, int totalCost) {
}
