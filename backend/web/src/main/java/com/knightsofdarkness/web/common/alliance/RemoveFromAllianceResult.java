package com.knightsofdarkness.web.common.alliance;

public record RemoveFromAllianceResult(String message, boolean success) {
    public static RemoveFromAllianceResult success(String message)
    {
        return new RemoveFromAllianceResult(message, true);
    }

    public static RemoveFromAllianceResult failure(String message)
    {
        return new RemoveFromAllianceResult(message, false);
    }
}
