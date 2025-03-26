package com.knightsofdarkness.web.common.alliance;

import java.util.Optional;

public record InviteAllianceResult(String message, boolean success, Optional<AllianceInvitationDto> invitation) {
    public static InviteAllianceResult success(String message, AllianceInvitationDto invitation)
    {
        return new InviteAllianceResult(message, true, Optional.of(invitation));
    }

    public static InviteAllianceResult failure(String message)
    {
        return new InviteAllianceResult(message, false, Optional.empty());
    }
}
