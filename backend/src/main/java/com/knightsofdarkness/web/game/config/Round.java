package com.knightsofdarkness.web.game.config;

/**
 * A round lasts a fixed number of turn ticks, one tick happens every newTurnPeriodicity milliseconds.
 */
public record Round(int lengthInTicks) {
}
