package com.uprzejmy.kod.kingdom;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.uprzejmy.kod.TestGame;
import com.uprzejmy.kod.game.Game;
import com.uprzejmy.kod.market.MarketResource;
import com.uprzejmy.kod.utils.KingdomBuilder;

class KingdomMarketTest
{
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
    void marketSanityTest()
    {
        var kingdom = kingdomBuilder.build();
        kingdom.postMarketOffer(MarketResource.food, 100, 100);

        assertEquals(1, kingdom.getMarketOffers().size());
    }
}
