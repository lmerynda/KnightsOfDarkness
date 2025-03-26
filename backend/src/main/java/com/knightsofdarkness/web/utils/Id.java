package com.knightsofdarkness.web.utils;

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
