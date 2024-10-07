package com.knightsofdarkness.common.kingdom;

import java.util.Optional;

public record SendCarriersResult(String message, boolean success, Optional<SendCarriersDto> data) {
    public static SendCarriersResult success(String message, SendCarriersDto data)
    {
        return new SendCarriersResult(message, true, Optional.of(data));
    }

    public static SendCarriersResult failure(String message)
    {
        return new SendCarriersResult(message, false, Optional.empty());
    }
}
