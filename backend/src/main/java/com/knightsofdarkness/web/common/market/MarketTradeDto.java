package com.knightsofdarkness.web.common.market;

/**
 * one sale on the market: who sold how much of what to whom, at what price a piece
 */
public record MarketTradeDto(MarketResource resource, String seller, String buyer, int price, int count) {
}
