package com.knightsofdarkness.web.common.alliance;

import java.util.Objects;

import com.knightsofdarkness.web.common.kingdom.UnitsMapDto;

/**
 * soldiers a member sends to stand guard at the alliance's monument
 */
public record MonumentGarrisonDto(UnitsMapDto units) {
    /** a request without these is refused as malformed, before it reaches the game */
    public MonumentGarrisonDto
    {
        Objects.requireNonNull(units, "units");
    }
}
