package com.knightsofdarkness.web.common.game;

import org.jspecify.annotations.Nullable;

import java.time.Instant;

public record RoundDto(int number, Instant startedAt, @Nullable Instant endedAt, int ticksElapsed, int lengthInTicks, long tickPeriodMillis, @Nullable RankingsDto finalRankings) {
}
