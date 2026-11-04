package com.knightsofdarkness.web.common.war;

import java.util.Objects;

public record SendSpiesDto(String targetKingdomName, int spies) {
    /** a request without these is refused as malformed, before it reaches the game */
    public SendSpiesDto
    {
        Objects.requireNonNull(targetKingdomName, "targetKingdomName");
    }
}
