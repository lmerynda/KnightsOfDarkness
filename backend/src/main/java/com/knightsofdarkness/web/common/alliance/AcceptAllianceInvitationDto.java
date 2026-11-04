package com.knightsofdarkness.web.common.alliance;

import java.util.Objects;
import java.util.UUID;

public record AcceptAllianceInvitationDto(UUID invitationId) {
    /** a request without these is refused as malformed, before it reaches the game */
    public AcceptAllianceInvitationDto
    {
        Objects.requireNonNull(invitationId, "invitationId");
    }
}
