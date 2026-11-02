package com.knightsofdarkness.web.common.kingdom;

import java.util.UUID;

/**
 * joins is an ally's army on the march against the same target; ours then waits at the border and fights right after it lands
 */
public record SendAttackDto(String destinationKingdomName, AttackType attackType, UnitsMapDto units, UUID joins) {
    public SendAttackDto(String destinationKingdomName, AttackType attackType, UnitsMapDto units)
    {
        this(destinationKingdomName, attackType, units, null);
    }
}
