package com.knightsofdarkness.common.alliance;

public record AcceptAllianceInvitationResult(String message, boolean success) {
    public static AcceptAllianceInvitationResult success(String message)
    {
        return new AcceptAllianceInvitationResult(message, true);
    }

    public static AcceptAllianceInvitationResult failure(String message)
    {
        return new AcceptAllianceInvitationResult(message, false);
    }
}
