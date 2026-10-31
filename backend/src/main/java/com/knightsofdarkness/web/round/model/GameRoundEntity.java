package com.knightsofdarkness.web.round.model;

import java.time.Instant;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.knightsofdarkness.web.common.game.RankingsDto;
import com.knightsofdarkness.web.common.game.RoundDto;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class GameRoundEntity {
    @Id
    int number;
    Instant startedAt;
    Instant endedAt;
    int ticksElapsed;
    int lengthInTicks;

    @JdbcTypeCode(SqlTypes.JSON)
    RankingsDto finalRankings;

    public GameRoundEntity()
    {
    }

    public GameRoundEntity(int number, Instant startedAt, int lengthInTicks)
    {
        this.number = number;
        this.startedAt = startedAt;
        this.lengthInTicks = lengthInTicks;
    }

    public RoundDto toDto(long tickPeriodMillis)
    {
        return new RoundDto(number, startedAt, endedAt, ticksElapsed, lengthInTicks, tickPeriodMillis, finalRankings);
    }

    /**
     * @return true when this tick ended the round
     */
    public boolean tick()
    {
        ticksElapsed++;
        return ticksElapsed >= lengthInTicks;
    }

    public void end(Instant now, RankingsDto rankings)
    {
        endedAt = now;
        finalRankings = rankings;
    }

    public int getNumber()
    {
        return number;
    }
}
