package com.knightsofdarkness.web.market.legacy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.common.market.MarketResource;
import com.knightsofdarkness.web.Game;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;
import com.knightsofdarkness.web.legacy.TestGame;
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
        market.buyExistingOffer(offer.get(), kingdom, kingdom, 1);

        Instant now = Instant.now();
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

        market.buyExistingOffer(offer.get(), kingdom, kingdom, 1);

        var now = Instant.now();
        var minuteAgo = now.minusSeconds(60);
        market.updateMarketTransactionsAverages(minuteAgo, now);
        double average = market.getLast24TransactionAverages(resource).orElse(0.0);
        assertEquals(50.0, average);
    }
}
