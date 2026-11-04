package com.knightsofdarkness.web.common.kingdom;

import java.util.Objects;
import java.util.UUID;

/**
 * joins is an ally's army on the march against the same target; ours then waits at the border and fights right after it lands
 */
public record SendAttackDto(String destinationKingdomName, AttackType attackType, UnitsMapDto units, UUID joins) {
    /** a request without these is refused as malformed, before it reaches the game */
    public SendAttackDto
    {
        Objects.requireNonNull(destinationKingdomName, "destinationKingdomName");
        Objects.requireNonNull(attackType, "attackType");
        Objects.requireNonNull(units, "units");
    }

    public SendAttackDto(String destinationKingdomName, AttackType attackType, UnitsMapDto units)
    {
        this(destinationKingdomName, attackType, units, null);
    }
}
