package com.knightsofdarkness.web.kingdom.model;

import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.knightsofdarkness.web.common.kingdom.AttackType;
import com.knightsofdarkness.web.common.kingdom.OngoingAttackDto;
import com.knightsofdarkness.web.common.kingdom.UnitsMapDto;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class KingdomOngoingAttackEntity {
    @Id
    UUID id;

    @ManyToOne
    @JoinColumn(name = "kingdom_name", nullable = false)
    KingdomEntity kingdom;

    String targetKingdomName;

    int turnsLeft;

    @Enumerated(EnumType.STRING)
    AttackType attackType;

    @JdbcTypeCode(SqlTypes.JSON)
    UnitsMapDto units;

    public KingdomOngoingAttackEntity()
    {
    }

    public KingdomOngoingAttackEntity(UUID id, KingdomEntity kingdom, String targetKingdomName, int turnsLeft, AttackType attackType, UnitsMapDto units)
    {
        this.id = id;
        this.kingdom = kingdom;
        this.targetKingdomName = targetKingdomName;
        this.turnsLeft = turnsLeft;
        this.attackType = attackType;
        this.units = units;
    }

    public OngoingAttackDto toDto()
    {
        OngoingAttackDto dto = new OngoingAttackDto();
        dto.id = id;
        dto.from = kingdom.getName();
        dto.to = targetKingdomName;
        dto.turnsLeft = turnsLeft;
        dto.attackType = attackType;
        dto.units = units;
        return dto;
    }

    public UUID getId()
    {
        return id;
    }
}
