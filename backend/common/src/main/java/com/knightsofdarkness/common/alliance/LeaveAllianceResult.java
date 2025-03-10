package com.knightsofdarkness.common.alliance;

public record LeaveAllianceResult(String message, boolean success) {
    public static LeaveAllianceResult success(String message)
    {
        return new LeaveAllianceResult(message, true);
    }

    public static LeaveAllianceResult failure(String message)
    {
        return new LeaveAllianceResult(message, false);
    }
}
