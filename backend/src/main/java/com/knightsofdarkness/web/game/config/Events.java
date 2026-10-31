package com.knightsofdarkness.web.game.config;

/**
 * Now and then a turn brings an event: a windfall adds a share of what the turn produced, a loss takes a share of the
 * stores, and bandits stay away from a realm with enough guards per acre.
 */
public record Events(double chancePerTurn, double windfallShare, double lossShare, double guardsPerAcreAgainstBandits) {
}
