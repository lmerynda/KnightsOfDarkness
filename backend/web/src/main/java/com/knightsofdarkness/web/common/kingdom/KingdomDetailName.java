package com.knightsofdarkness.web.common.kingdom;

public enum KingdomDetailName
{
    usedLand;

    public static KingdomDetailName from(String detail)
    {
        return switch (detail)
        {
            case "usedLand" -> usedLand;
            default -> throw new IllegalArgumentException("Unknown detail: " + detail);
        };
    }
}
