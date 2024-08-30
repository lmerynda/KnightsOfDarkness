package com.knightsofdarkness.game;

import java.util.UUID;

public final class Id {
    private Id()
    {
    }

    public static UUID generate()
    {
        return UUID.randomUUID();
    }
}
