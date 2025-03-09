package com.knightsofdarkness.common.alliance;

import java.util.Optional;

public record CreateAllianceResult(String message, boolean success, Optional<AllianceDto> turnReport) {
    public static CreateAllianceResult success(String message, AllianceDto report)
    {
        return new CreateAllianceResult(message, true, Optional.of(report));
    }

    public static CreateAllianceResult failure(String message)
    {
        return new CreateAllianceResult(message, false, Optional.empty());
    }
}
