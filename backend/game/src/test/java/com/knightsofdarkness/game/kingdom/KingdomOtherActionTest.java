package com.knightsofdarkness.game.kingdom;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class KingdomOtherActionTest {
    @Test
    void testBuyLand_101()
    {
        var transaction = KingdomOtherAction.calculateCost(100, 1, 5000);
        assertEquals(1, transaction.amount());
        assertEquals(1020, transaction.cost());
    }

    @Test
    void testBuyLand_1001_withoutGold()
    {
        var transaction = KingdomOtherAction.calculateCost(1000, 1, 5000);
        assertEquals(0, transaction.amount());
        assertEquals(0, transaction.cost());
    }

    @Test
    void testBuyLand_1001_withGold()
    {
        var transaction = KingdomOtherAction.calculateCost(1000, 1, 99999999);
        assertEquals(1, transaction.amount());
        assertEquals(100200, transaction.cost());
    }

    @Test
    void testBuyLand_from100_to200()
    {
        var transaction = KingdomOtherAction.calculateCost(1000, 200, 99999999);
        assertEquals(200, transaction.amount());
        assertEquals(24288680, transaction.cost());
    }
}
