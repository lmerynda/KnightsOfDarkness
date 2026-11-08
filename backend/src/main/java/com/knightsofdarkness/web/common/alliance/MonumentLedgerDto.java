package com.knightsofdarkness.web.common.alliance;

import org.jspecify.annotations.Nullable;

/**
 * what only the alliance's members see of their monument: what its store holds, what the next stage still costs (nothing
 * once it stands complete), and what it eats each tick
 */
public record MonumentLedgerDto(MonumentDto monument, MonumentGoodsDto store, @Nullable MonumentGoodsDto nextStageCost, MonumentGoodsDto upkeep, boolean upkeepUnpaid) {
}
