package com.knightsofdarkness.game;

import java.util.UUID;

public class Id {
    private Id()
    {
    }

    public static UUID generate()
    {
        return UUID.randomUUID();
    }
}
