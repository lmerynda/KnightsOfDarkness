package com.knightsofdarkness.web.common.kingdom;

import java.util.Objects;

public record SendReinforcementsDto(String hostKingdomName, UnitsMapDto units) {
    /** a request without these is refused as malformed, before it reaches the game */
    public SendReinforcementsDto
    {
        Objects.requireNonNull(hostKingdomName, "hostKingdomName");
        Objects.requireNonNull(units, "units");
    }
}
