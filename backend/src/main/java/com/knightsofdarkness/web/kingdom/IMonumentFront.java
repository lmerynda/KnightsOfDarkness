package com.knightsofdarkness.web.kingdom;

import java.util.Optional;

import com.knightsofdarkness.web.monument.model.MonumentEntity;

/**
 * What the war needs to know of monuments: where they stand, and who should hear how an assault went.
 */
public interface IMonumentFront {
    boolean standsAt(String seat);

    /**
     * the monument seated in this kingdom, held until the transaction ends, as members pay in and the clock takes its upkeep
     */
    Optional<MonumentEntity> findAtSeatForUpdate(String seat);

    /**
     * @param brokeTheHold whether the monument stood complete before the assault, so that its hold must start again
     */
    void assaultWon(MonumentEntity monument, String attacker, boolean brokeTheHold);

    void assaultRepelled(MonumentEntity monument, String attacker);
}
