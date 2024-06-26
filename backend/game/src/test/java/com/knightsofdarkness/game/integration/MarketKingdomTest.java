package com.knightsofdarkness.game.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.game.Game;
import com.knightsofdarkness.game.TestGame;
import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.game.market.IMarket;
import com.knightsofdarkness.game.market.MarketResource;
import com.knightsofdarkness.game.utils.KingdomBuilder;

public class MarketKingdomTest {
    private Game game;
    private IMarket market;
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
        market.addOffer(kingdom, MarketResource.food, 100, 100);
        var foodOffers = market.getOffersByResource(MarketResource.food);
        assertEquals(1, foodOffers.size());
        assertEquals(kingdom, foodOffers.get(0).getSeller());
    }
}
