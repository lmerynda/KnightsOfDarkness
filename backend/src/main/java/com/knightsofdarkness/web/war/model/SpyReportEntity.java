package com.knightsofdarkness.web.war.model;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.knightsofdarkness.web.common.war.SpyReportDto;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class SpyReportEntity {
    @Id
    UUID id;
    String kingdomName;
    Instant date;

    @JdbcTypeCode(SqlTypes.JSON)
    SpyReportDto report;

    public SpyReportEntity()
    {
    }

    public SpyReportEntity(SpyReportDto report)
    {
        this.id = report.id();
        this.kingdomName = report.kingdomName();
        this.date = report.date();
        this.report = report;
    }

    public SpyReportDto toDto()
    {
        return report;
    }
}
