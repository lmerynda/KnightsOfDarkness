package com.knightsofdarkness.web.war.model;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.knightsofdarkness.web.common.war.BattleReportDto;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class BattleReportEntity {
    @Id
    UUID id;
    String attacker;
    String defender;
    Instant date;

    @JdbcTypeCode(SqlTypes.JSON)
    BattleReportDto report;

    public BattleReportEntity()
    {
    }

    public BattleReportEntity(BattleReportDto report)
    {
        this.id = report.id();
        this.attacker = report.attacker();
        this.defender = report.defender();
        this.date = report.date();
        this.report = report;
    }

    public BattleReportDto toDto()
    {
        return report;
    }
}
