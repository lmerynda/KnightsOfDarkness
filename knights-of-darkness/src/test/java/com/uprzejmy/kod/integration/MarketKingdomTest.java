package com.uprzejmy.kod.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.uprzejmy.kod.TestGame;
import com.uprzejmy.kod.game.Game;
import com.uprzejmy.kod.kingdom.Kingdom;
import com.uprzejmy.kod.market.Market;
import com.uprzejmy.kod.market.MarketResource;
import com.uprzejmy.kod.utils.KingdomBuilder;

public class MarketKingdomTest
{
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
