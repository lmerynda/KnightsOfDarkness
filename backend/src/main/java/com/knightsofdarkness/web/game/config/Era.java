package com.knightsofdarkness.web.game.config;

/**
 * A stalled era ends at the latest after this many turn ticks, one tick happens every newTurnPeriodicity milliseconds.
 */
public record Era(int maxLengthInTicks) {
}
