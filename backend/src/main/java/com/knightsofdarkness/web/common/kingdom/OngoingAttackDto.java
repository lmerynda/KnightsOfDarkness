package com.knightsofdarkness.web.common.kingdom;

import org.jspecify.annotations.Nullable;

import java.util.UUID;

public class OngoingAttackDto {
    public UUID id;
    public String from;
    public String to;
    public int turnsLeft;
    public AttackType attackType;
    public UnitsMapDto units;
    /** the ally whose strike this army joins, or null when it marches alone */
    public @Nullable String joining;
    /** how many of its owner's turns a joined army has waited at the border for its lead */
    public int turnsAtBorder;
}
