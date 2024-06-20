package com.knightsofdarkness.game.kingdom;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.game.Game;
import com.knightsofdarkness.game.TestGame;
import com.knightsofdarkness.game.market.MarketResource;
import com.knightsofdarkness.game.utils.KingdomBuilder;

class KingdomMarketTest {
    private static Game game;
    private KingdomBuilder kingdomBuilder;

    @BeforeAll
    static void beforeAll()
    {
        game = new TestGame().get();
    }

    @BeforeEach
    void setUp()
    {
        this.kingdomBuilder = new KingdomBuilder(game);
    }

    @Test
    @Disabled
    void marketSanityTest()
    {
        var kingdom = kingdomBuilder.build();
        game.getMarket().addOffer(kingdom, MarketResource.food, 100, 100);
        assertEquals(1, kingdom.getMarketOffers().size());
    }
}
