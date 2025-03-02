package com.knightsofdarkness.game.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.common.market.MarketResource;
import com.knightsofdarkness.web.kingdom.legacy.Kingdom;
import com.knightsofdarkness.web.legacy.Game;
import com.knightsofdarkness.web.legacy.TestGame;
import com.knightsofdarkness.web.legacy.utils.KingdomBuilder;
import com.knightsofdarkness.web.market.IMarket;

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
        game.addKingdom(kingdom);
    }

    @Test
    void whenKingdomMakesOneOffer_thenTheSameOfferShouldAppearOnMarket()
    {
        var result = market.createOffer(kingdom.getName(), MarketResource.food, 100, 100);
        assertEquals(true, result.success());
        var foodOffers = market.getOffersByResource(MarketResource.food);
        assertEquals(1, foodOffers.size());
        assertEquals(kingdom, foodOffers.get(0).getSeller());
    }
}
