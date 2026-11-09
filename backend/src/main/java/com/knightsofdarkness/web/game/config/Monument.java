package com.knightsofdarkness.web.game.config;

/**
 * What an alliance's monument takes: its stages, the goods each stage costs, what every stage eats each tick, and the
 * share of a stage it loses in a tick its store cannot pay, and the most of its stages a crushing assault knocks down.
 */
public record Monument(int stages, StageCost stageCost, Upkeep upkeepPerStage, double decayRatio, double maxRazeRatio) {
    public record StageCost(long gold, long iron, long tools) {
    }

    public record Upkeep(long gold, long food) {
    }
}
