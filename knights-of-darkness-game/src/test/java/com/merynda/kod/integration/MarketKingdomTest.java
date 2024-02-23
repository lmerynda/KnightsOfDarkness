package com.merynda.kod.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.merynda.kod.TestGame;
import com.merynda.kod.game.Game;
import com.merynda.kod.kingdom.Kingdom;
import com.merynda.kod.market.Market;
import com.merynda.kod.market.MarketResource;
import com.merynda.kod.utils.KingdomBuilder;

public class MarketKingdomTest {
    private Game game;
    private Market market;
    private Kingdom kingdom;

    @BeforeEach
    void setUp()
    {
        game = new TestGame().get();
        market = game.getMarket();
        kingdom = new KingdomBuilder(game).build();
    }

    @Test
    void whenKingdomMakesOneOffer_thenTheSameOfferShouldAppearOnMarket()
    {
        kingdom.postMarketOffer(MarketResource.food, 100, 100);
        var foodOffers = market.getOffersByResource(MarketResource.food);
        assertEquals(1, foodOffers.size());
        assertEquals(kingdom, foodOffers.get(0).getKingdom());
    }
}
