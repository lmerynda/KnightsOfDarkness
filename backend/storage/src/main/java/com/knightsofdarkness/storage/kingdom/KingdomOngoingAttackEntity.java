package com.knightsofdarkness.storage.kingdom;

import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.knightsofdarkness.common.kingdom.AttackType;
import com.knightsofdarkness.common.kingdom.OngoingAttackDto;
import com.knightsofdarkness.common.kingdom.UnitsMapDto;
import com.knightsofdarkness.game.kingdom.KingdomOngoingAttack;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class KingdomOngoingAttackEntity {
    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "kingdom_name", nullable = false)
    private KingdomEntity kingdom;

    private String targetKingdomName;

    private int turnsLeft;

    @Enumerated(EnumType.STRING)
    private AttackType attackType;

    @JdbcTypeCode(SqlTypes.JSON)
    public UnitsMapDto units;

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

    public KingdomOngoingAttack toDomainModel()
    {
        return new KingdomOngoingAttack(id, targetKingdomName, turnsLeft, attackType, units);
    }

    public static KingdomOngoingAttackEntity fromDomainModel(KingdomOngoingAttack ongoingAttack, KingdomEntity kingdom)
    {
        return new KingdomOngoingAttackEntity(ongoingAttack.getId(), kingdom, ongoingAttack.getTargetKingdomName(), ongoingAttack.getTurnsLeft(), ongoingAttack.getAttackType(), ongoingAttack.getUnits());
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
}
