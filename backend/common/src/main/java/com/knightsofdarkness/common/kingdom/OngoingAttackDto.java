package com.knightsofdarkness.common.kingdom;

import java.util.UUID;

public class OngoingAttackDto {
    public UUID id;
    public String from;
    public String to;
    public int turnsLeft;
    public AttackType attackType;
    public UnitsMapDto units;
}
