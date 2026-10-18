package com.knightsofdarkness.web.market.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.web.Game;
import com.knightsofdarkness.web.common.market.MarketResource;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;
import com.knightsofdarkness.web.legacy.TestGame;
import com.knightsofdarkness.web.market.IMarket;
import com.knightsofdarkness.web.utils.KingdomBuilder;

class MarketTransactionsDataTest {
    private Game game;
    private IMarket market;
    private KingdomEntity kingdom;

    @BeforeEach
    void beforeEach()
    {
        game = new TestGame().get();
        kingdom = new KingdomBuilder(game).build();
        game.addKingdom(kingdom);
        market = game.getMarket();
    }

    @Test
    void testMarketBuyRegistersTransaction()
    {
        var resource = MarketResource.food;
        var result = market.createOffer(kingdom.getName(), resource, 1, 50);
        assertTrue(result.success());
        var offer = market.findOfferById(result.data().get().id());
        assertTrue(offer.isPresent());
        Instant now = Instant.parse("2026-01-01T12:00:00Z");
        Instant transactionTime = now.minusSeconds(1);
        try (var clock = mockStatic(Instant.class, CALLS_REAL_METHODS))
        {
            clock.when(Instant::now).thenReturn(transactionTime);
            market.buyExistingOffer(offer.get(), kingdom, kingdom, 1);
        }
        Instant minuteAgo = now.minusSeconds(60);
        var transactions = market.getTransactionsByResourceAndTimeRange(resource, minuteAgo, now);
        assertThat(transactions).isNotEmpty();
        var lastTransaction = transactions.get(0);

        assertEquals(50, lastTransaction.price);
        assertEquals(1, lastTransaction.count);
    }

    @Test
    void testTransactionsAveragesData()
    {
        var resource = MarketResource.food;
        var result = market.createOffer(kingdom.getName(), resource, 1, 50);
        assertTrue(result.success());
        var offer = market.findOfferById(result.data().get().id());
        assertTrue(offer.isPresent());

        var now = Instant.parse("2026-01-01T12:00:00Z");
        var transactionTime = now.minusSeconds(1);
        try (var clock = mockStatic(Instant.class, CALLS_REAL_METHODS))
        {
            clock.when(Instant::now).thenReturn(transactionTime);
            market.buyExistingOffer(offer.get(), kingdom, kingdom, 1);
        }
        var minuteAgo = now.minusSeconds(60);
        market.updateMarketTransactionsAverages(minuteAgo, now);
        double average = market.getLast24TransactionAverages(resource).orElse(0.0);
        assertEquals(50.0, average);
    }
}
