package com.knightsofdarkness.web.common.alliance;

public record RejectAllianceInvitationResult(String message, boolean success) {
    public static RejectAllianceInvitationResult success(String message)
    {
        return new RejectAllianceInvitationResult(message, true);
    }

    public static RejectAllianceInvitationResult failure(String message)
    {
        return new RejectAllianceInvitationResult(message, false);
    }
}
