package com.knightsofdarkness.web.common.game;

import org.jspecify.annotations.Nullable;

import java.time.Instant;

public record EraDto(int number, Instant startedAt, @Nullable Instant endedAt, int ticksElapsed, int maxLengthInTicks, long tickPeriodMillis, @Nullable String winningAlliance, @Nullable EraEnding endedBy, @Nullable RankingsDto finalRankings) {
}
