package com.knightsofdarkness.web.common.alliance;

import java.util.Optional;

public record CreateAllianceResult(String message, boolean success, Optional<AllianceDto> alliance) {
    public static CreateAllianceResult success(String message, AllianceDto alliance)
    {
        return new CreateAllianceResult(message, true, Optional.of(alliance));
    }

    public static CreateAllianceResult failure(String message)
    {
        return new CreateAllianceResult(message, false, Optional.empty());
    }
}
