package com.knightsofdarkness.common.alliance;

public record CreateAllianceResult(String message, boolean success) {
    public static CreateAllianceResult success(String message)
    {
        return new CreateAllianceResult(message, true);
    }

    public static CreateAllianceResult failure(String message)
    {
        return new CreateAllianceResult(message, false);
    }

}
