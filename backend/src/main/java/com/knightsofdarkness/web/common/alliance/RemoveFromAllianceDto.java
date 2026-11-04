package com.knightsofdarkness.web.common.alliance;

import java.util.Objects;

public record RemoveFromAllianceDto(String kingdomName) {
    /** a request without these is refused as malformed, before it reaches the game */
    public RemoveFromAllianceDto
    {
        Objects.requireNonNull(kingdomName, "kingdomName");
    }
}
