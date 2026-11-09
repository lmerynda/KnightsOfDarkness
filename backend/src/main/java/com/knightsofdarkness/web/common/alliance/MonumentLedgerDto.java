package com.knightsofdarkness.web.common.alliance;

import java.util.List;

import org.jspecify.annotations.Nullable;

import com.knightsofdarkness.web.common.kingdom.ReinforcementDto;

/**
 * what only the alliance's members see of their monument: what its store holds, what the next stage still costs (nothing
 * once it stands complete), what it eats each tick, and whose soldiers stand guard at it or march there
 */
public record MonumentLedgerDto(MonumentDto monument, MonumentGoodsDto store, @Nullable MonumentGoodsDto nextStageCost, MonumentGoodsDto upkeep, boolean upkeepUnpaid, List<ReinforcementDto> garrison) {
}
