package com.knightsofdarkness.web.common.alliance;

public record AddBotToAllianceResult(String message, boolean success) {
    public static AddBotToAllianceResult success(String message)
    {
        return new AddBotToAllianceResult(message, true);
    }

    public static AddBotToAllianceResult failure(String message)
    {
        return new AddBotToAllianceResult(message, false);
    }
}
