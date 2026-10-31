package com.knightsofdarkness.web.common.messaging;

public record SendMessageResult(String message, boolean success, int delivered) {
    public static SendMessageResult success(String message, int delivered)
    {
        return new SendMessageResult(message, true, delivered);
    }

    public static SendMessageResult failure(String message)
    {
        return new SendMessageResult(message, false, 0);
    }
}
