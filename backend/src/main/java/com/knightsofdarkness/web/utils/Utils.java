package com.knightsofdarkness.web.utils;

import java.util.Arrays;
import java.util.Locale;

import org.slf4j.helpers.MessageFormatter;

public final class Utils {
    private Utils()
    {
    }

    /**
     * fills {} placeholders in order; whole numbers are grouped by thousands, because players read these messages
     */
    public static String format(String template, Object... args)
    {
        var readable = Arrays.stream(args).map(arg -> arg instanceof Integer || arg instanceof Long ? String.format(Locale.ENGLISH, "%,d", arg) : arg).toArray();
        return MessageFormatter.arrayFormat(template, readable).getMessage();
    }
}
