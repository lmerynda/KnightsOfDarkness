package com.knightsofdarkness.common.alliance;

public record InviteAllianceResult(String message, boolean success) {
    public static InviteAllianceResult success(String message)
    {
        return new InviteAllianceResult(message, true);
    }

    public static InviteAllianceResult failure(String message)
    {
        return new InviteAllianceResult(message, false);
    }
}
