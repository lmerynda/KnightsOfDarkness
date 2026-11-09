package com.knightsofdarkness.web.era.model;

import java.time.Instant;

import org.hibernate.annotations.JdbcTypeCode;
import org.jspecify.annotations.Nullable;
import org.hibernate.type.SqlTypes;

import com.knightsofdarkness.web.common.game.EraDto;
import com.knightsofdarkness.web.common.game.EraEnding;
import com.knightsofdarkness.web.common.game.RankingsDto;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

@Entity
public class GameEraEntity {
    @Id
    int number;
    Instant startedAt;
    Instant endedAt;
    int ticksElapsed;
    int maxLengthInTicks;
    String winningAlliance;

    @Enumerated(EnumType.STRING)
    EraEnding endedBy;

    @JdbcTypeCode(SqlTypes.JSON)
    RankingsDto finalRankings;

    public GameEraEntity()
    {
    }

    public GameEraEntity(int number, Instant startedAt, int maxLengthInTicks)
    {
        this.number = number;
        this.startedAt = startedAt;
        this.maxLengthInTicks = maxLengthInTicks;
    }

    public EraDto toDto(long tickPeriodMillis)
    {
        return new EraDto(number, startedAt, endedAt, ticksElapsed, maxLengthInTicks, tickPeriodMillis, winningAlliance, endedBy, finalRankings);
    }

    /**
     * @return true when this tick ran the era out of time
     */
    public boolean tick()
    {
        ticksElapsed++;
        return ticksElapsed >= maxLengthInTicks;
    }

    public void end(Instant now, EraEnding ending, @Nullable String winner, RankingsDto rankings)
    {
        endedAt = now;
        endedBy = ending;
        finalRankings = rankings;
        winningAlliance = winner;
    }

    public int getNumber()
    {
        return number;
    }
}
