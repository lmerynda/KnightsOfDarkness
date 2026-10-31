package com.knightsofdarkness.web.common.kingdom;

public record SendReinforcementsResult(String message, boolean success) {
    public static SendReinforcementsResult success(String message)
    {
        return new SendReinforcementsResult(message, true);
    }

    public static SendReinforcementsResult failure(String message)
    {
        return new SendReinforcementsResult(message, false);
    }
}
