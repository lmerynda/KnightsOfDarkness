package com.knightsofdarkness.game.kingdom;

import java.util.UUID;

import com.knightsofdarkness.common.kingdom.AttackType;
import com.knightsofdarkness.common.kingdom.UnitsMapDto;

public class KingdomOngoingAttack {
    UUID id;
    String targetKingdomName;
    int turnsLeft;
    AttackType attackType;
    UnitsMapDto units;

    public KingdomOngoingAttack(UUID id, String targetKingdomName, int turnsLeft, AttackType attackType, UnitsMapDto units)
    {
        this.id = id;
        this.targetKingdomName = targetKingdomName;
        this.turnsLeft = turnsLeft;
        this.attackType = attackType;
        this.units = units;
    }

    public UUID getId()
    {
        return id;
    }

    public String getTargetKingdomName()
    {
        return targetKingdomName;
    }

    public int getTurnsLeft()
    {
        return turnsLeft;
    }

    public AttackType getAttackType()
    {
        return attackType;
    }

    public UnitsMapDto getUnits()
    {
        return units;
    }

    @Override
    public String toString()
    {
        return "KingdomOngoingAttack{" +
                "id=" + id +
                ", targetKingdomName='" + targetKingdomName + '\'' +
                ", turnsLeft=" + turnsLeft +
                ", attackType=" + attackType +
                ", units=" + units +
                '}';
    }
}
