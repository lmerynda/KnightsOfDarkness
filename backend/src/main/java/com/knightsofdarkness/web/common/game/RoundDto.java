package com.knightsofdarkness.web.common.game;

import java.time.Instant;

public record RoundDto(int number, Instant startedAt, Instant endedAt, int ticksElapsed, int lengthInTicks, long tickPeriodMillis, RankingsDto finalRankings) {
}
