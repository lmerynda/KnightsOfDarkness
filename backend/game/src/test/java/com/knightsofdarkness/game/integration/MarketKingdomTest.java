package com.knightsofdarkness.game.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.common.market.MarketResource;
import com.knightsofdarkness.game.Game;
import com.knightsofdarkness.game.TestGame;
import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.game.market.IMarket;
import com.knightsofdarkness.game.utils.KingdomBuilder;

class MarketKingdomTest {
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

    @Disabled
    @Test
    void whenKingdomMakesOneOffer_thenTheSameOfferShouldAppearOnMarket()
    {
        market.createOffer(kingdom.getName(), MarketResource.food, 100, 100);
        var foodOffers = market.getOffersByResource(MarketResource.food);
        assertEquals(1, foodOffers.size());
        assertEquals(kingdom, foodOffers.get(0).getSeller());
    }
}
