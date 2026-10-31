package com.knightsofdarkness.web.common.war;

import java.util.Optional;

public record SendSpiesResult(String message, boolean success, Optional<SpyReportDto> report) {
    public static SendSpiesResult success(String message, SpyReportDto report)
    {
        return new SendSpiesResult(message, true, Optional.of(report));
    }

    public static SendSpiesResult failure(String message)
    {
        return new SendSpiesResult(message, false, Optional.empty());
    }
}
