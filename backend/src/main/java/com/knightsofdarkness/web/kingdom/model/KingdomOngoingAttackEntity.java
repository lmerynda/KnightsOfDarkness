package com.knightsofdarkness.web.kingdom.model;

import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.knightsofdarkness.web.common.kingdom.AttackType;
import com.knightsofdarkness.web.common.kingdom.IncomingArmyDto;
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

    // the ally's army this one joins: it waits at the border and fights right after that army lands
    UUID joinsAttackId;

    String joinsKingdomName;

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
        dto.joining = joinsKingdomName;
        return dto;
    }

    /**
     * the army as its target's scouts see it: the size only to two significant figures
     */
    public IncomingArmyDto toIncomingDto()
    {
        return new IncomingArmyDto(kingdom.getName(), targetKingdomName, attackType, turnsLeft, roughly(units.countMilitary()));
    }

    static int roughly(int count)
    {
        if (count < 100)
        {
            return count;
        }
        var scale = (int) Math.pow(10, (int) Math.log10(count) - 1);
        return Math.round((float) count / scale) * scale;
    }

    public UUID getId()
    {
        return id;
    }

    public KingdomEntity getKingdom()
    {
        return kingdom;
    }

    public String getTargetKingdomName()
    {
        return targetKingdomName;
    }

    public UUID getJoinsAttackId()
    {
        return joinsAttackId;
    }

    /**
     * a joining army joins the strike's lead, never another joiner, so every strike has one army that sets off the rest
     */
    void join(KingdomOngoingAttackEntity lead)
    {
        this.joinsAttackId = lead.id;
        this.joinsKingdomName = lead.kingdom.getName();
    }

    public boolean joins(KingdomOngoingAttackEntity lead)
    {
        return lead.id.equals(joinsAttackId);
    }
}
