package com.knightsofdarkness.web.common.kingdom;

import java.util.UUID;

public class OngoingAttackDto {
    public UUID id;
    public String from;
    public String to;
    public int turnsLeft;
    public AttackType attackType;
    public UnitsMapDto units;
    /** the ally whose strike this army joins, or null when it marches alone */
    public String joining;
}
