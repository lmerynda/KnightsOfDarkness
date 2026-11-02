package com.knightsofdarkness.web.common;

import java.util.Optional;

/**
 * what every action answers: whether it went through, a message to show the player, and what it produced, if anything
 */
public record ActionResult<T>(String message, boolean success, Optional<T> data) {
    public static <T> ActionResult<T> success(String message)
    {
        return new ActionResult<>(message, true, Optional.empty());
    }

    public static <T> ActionResult<T> success(String message, T data)
    {
        return new ActionResult<>(message, true, Optional.of(data));
    }

    public static <T> ActionResult<T> failure(String message)
    {
        return new ActionResult<>(message, false, Optional.empty());
    }
}
