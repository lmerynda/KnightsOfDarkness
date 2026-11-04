package com.knightsofdarkness.web.common.alliance;

import java.util.Objects;
import java.util.UUID;

public record AllianceInvitationDto(UUID id, String kingdomName, String allianceName) {
    /** a request without these is refused as malformed, before it reaches the game */
    public AllianceInvitationDto
    {
        Objects.requireNonNull(kingdomName, "kingdomName");
        Objects.requireNonNull(allianceName, "allianceName");
    }
}
