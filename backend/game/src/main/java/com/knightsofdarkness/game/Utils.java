package com.knightsofdarkness.game;

import org.slf4j.helpers.MessageFormatter;

public final class Utils {
    private Utils()
    {
    }

    public static String format(String template, Object... args)
    {
        return MessageFormatter.arrayFormat(template, args).getMessage();
    }
}
