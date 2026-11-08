package com.knightsofdarkness.web.common.alliance;

/**
 * goods in or for a monument; a monument's store and costs outgrow what a kingdom can hold, hence the long counts
 */
public record MonumentGoodsDto(long gold, long iron, long tools, long food) {
}
