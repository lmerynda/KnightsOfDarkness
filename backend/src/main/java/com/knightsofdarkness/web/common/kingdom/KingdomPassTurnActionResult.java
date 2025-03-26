package com.knightsofdarkness.web.common.kingdom;

import java.util.Optional;

public record KingdomPassTurnActionResult(String message, boolean success, Optional<KingdomTurnReport> turnReport) {
    public static KingdomPassTurnActionResult success(String message, KingdomTurnReport report)
    {
        return new KingdomPassTurnActionResult(message, true, Optional.of(report));
    }

    public static KingdomPassTurnActionResult failure(String message)
    {
        return new KingdomPassTurnActionResult(message, false, Optional.empty());
    }
}
