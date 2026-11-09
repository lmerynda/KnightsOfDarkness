package com.knightsofdarkness.web.common.alliance;

import org.jspecify.annotations.Nullable;

/**
 * what everyone sees of a monument, its garrison only to two significant figures; a complete one shows how many more
 * ticks it must stand to win the era
 */
public record MonumentDto(String allianceName, String seat, double stages, int totalStages, boolean complete, @Nullable Integer ticksToWin, int garrison) {
}
