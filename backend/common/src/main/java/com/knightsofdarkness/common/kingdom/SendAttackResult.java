package com.knightsofdarkness.common.kingdom;

import java.util.Optional;

public record SendAttackResult(String message, boolean success, Optional<SendAttackDto> data) {
    public static SendAttackResult success(String message, SendAttackDto data)
    {
        return new SendAttackResult(message, true, Optional.of(data));
    }

    public static SendAttackResult failure(String message)
    {
        return new SendAttackResult(message, false, Optional.empty());
    }
}
