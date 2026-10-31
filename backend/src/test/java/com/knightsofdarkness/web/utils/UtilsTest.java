package com.knightsofdarkness.web.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UtilsTest
{
    @Test
    void format_shouldGroupWholeNumbersForReaders()
    {
        assertEquals("Bandits robbed 191,475 gold from Umbar, 3 guards short", Utils.format("Bandits robbed {} gold from {}, {} guards short", 191475, "Umbar", 3));
    }
}
