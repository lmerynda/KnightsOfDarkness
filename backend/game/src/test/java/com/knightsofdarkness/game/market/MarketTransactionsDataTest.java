package com.knightsofdarkness.game.market;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.game.Game;
import com.knightsofdarkness.game.Id;
import com.knightsofdarkness.game.TestGame;
import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.game.utils.KingdomBuilder;
import com.knightsofdarkness.game.utils.MarketBuilder;

class MarketTransactionsDataTest {
    private Game game;
    private IMarket market;
    private Kingdom kingdom;

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
        var offer = market.addOffer(kingdom, resource, 1, 50);
        market.buyExistingOffer(offer, kingdom, kingdom, 1);

        Instant now = Instant.now();
        Instant minuteAgo = now.minusSeconds(60);
        var transactions = market.getTransactionsByResourceAndTimeRange(resource, minuteAgo, now);
        assertThat(transactions.size()).isGreaterThanOrEqualTo(1);
        var lastTransaction = transactions.get(0);

        assertEquals(50, lastTransaction.price);
        assertEquals(1, lastTransaction.count);
    }

    @Test
    void testTransactionsAveragesData()
    {
        var offer = market.addOffer(kingdom, MarketResource.food, 1, 50);
        market.buyExistingOffer(offer, kingdom, kingdom, 1); // TODO implement

        var now = Instant.now();
        var minuteAgo = now.minusSeconds(60);
        market.updateMarketTransactionsAverages(minuteAgo, now); // TODO implement
        var average = market.getLast24TransactionAverages(MarketResource.food);
        assertThat(average).isEqualTo(0.0); // TODO should be assert below, but implement test stubs first
        // assertThat(average).isGreaterThan(0.0);
    }

    @Disabled
    @Test
    void testThreeOffers()
    {
        var kingdom = new KingdomBuilder(game).build();
        IMarket market = new MarketBuilder().build();

        market.addOffer(kingdom, MarketResource.food, 100, 50);
        market.addOffer(kingdom, MarketResource.food, 100, 50);
        market.addOffer(kingdom, MarketResource.food, 100, 50);
        market.addOffer(kingdom, MarketResource.food, 100, 51);
        market.addOffer(kingdom, MarketResource.food, 100, 52);

        assertEquals(5, market.getOffersByResource(MarketResource.food).size());
    }

    @Disabled
    @Test
    void testRemoveOffer()
    {
        var kingdom = new KingdomBuilder(game).build();
        IMarket market = new MarketBuilder().build();
        var offer = market.addOffer(kingdom, MarketResource.food, 100, 50);

        market.removeOffer(offer);

        assertEquals(0, market.getOffersByResource(MarketResource.food).size());
    }

    @Disabled
    @Test
    void testRemoveThreeOffers()
    {
        var kingdom = new KingdomBuilder(game).build();
        IMarket market = new MarketBuilder().build();
        var offer1 = market.addOffer(kingdom, MarketResource.food, 100, 50);
        var offer2 = market.addOffer(kingdom, MarketResource.food, 100, 50);
        var offer3 = market.addOffer(kingdom, MarketResource.food, 100, 51);
        market.addOffer(kingdom, MarketResource.food, 100, 51);
        market.addOffer(kingdom, MarketResource.food, 100, 51);

        market.removeOffer(offer1);
        market.removeOffer(offer2);
        market.removeOffer(offer3);

        assertEquals(2, market.getOffersByResource(MarketResource.food).size());
    }

    @Test
    @Disabled
    void buyingOffer_whenNoOffersExist_shouldNotBuyAnything()
    {
        var kingdom = new KingdomBuilder(game).build();
        IMarket market = new MarketBuilder().build();

        var offer = new MarketOffer(Id.generate(), kingdom, MarketResource.food, 100, 100);

        var amountBought = market.buyExistingOffer(offer, kingdom, kingdom, 100);

        assertEquals(0, market.getOffersByResource(MarketResource.food).size());
        assertEquals(0, amountBought);
    }

    @Disabled
    @Test
    void buyingOffer_whenOneOfferExistsAndHasEnoughAmount_shouldSellEntireRequestedAmountAndStillHasOfferAvailable()
    {
        var kingdom = new KingdomBuilder(game).build();
        IMarket market = new MarketBuilder().build();

        market.addOffer(kingdom, MarketResource.food, 111, 100);
        var offers = market.getOffersByResource(MarketResource.food);
        assertTrue(!offers.isEmpty());

        var amountBought = market.buyExistingOffer(offers.get(0), kingdom, kingdom, 100);

        assertEquals(1, market.getOffersByResource(MarketResource.food).size());
        assertEquals(100, amountBought);
    }

    @Test
    @Disabled
    void buyingOffer_whenOneOfferExistsAndHasExactlyTheSameAmount_shouldSellEntireRequestedAmountAndHasNoOffersAvailable()
    {
        var kingdom = new KingdomBuilder(game).build();
        IMarket market = new MarketBuilder().build();

        market.addOffer(kingdom, MarketResource.food, 100, 100);
        var offers = market.getOffersByResource(MarketResource.food);
        assertTrue(!offers.isEmpty());

        var amountBought = market.buyExistingOffer(offers.get(0), kingdom, kingdom, 100);

        assertEquals(0, market.getOffersByResource(MarketResource.food).size());
        assertEquals(100, amountBought);
    }

    @Disabled
    @Test
    void buyingOffer_whenOfferExist_shouldReduceTheAmountStillAvailableByTheAmountBought()
    {
        var kingdom = new KingdomBuilder(game).build();
        IMarket market = new MarketBuilder().build();

        market.addOffer(kingdom, MarketResource.food, 100, 100);
        var offers = market.getOffersByResource(MarketResource.food);
        assertTrue(!offers.isEmpty());
        var offer = offers.get(0);

        var result = market.buyExistingOffer(offer, kingdom, kingdom, 20);

        assertEquals(1, market.getOffersByResource(MarketResource.food).size());
        assertEquals(100 - result.count, offer.getCount());
    }

    @Test
    @Disabled
    void buyingOffer_whenMultipleOffersExistAndBuyersTakeTheEntireOne_shouldReduceNumberOfAvailableOffersByOne()
    {
        var kingdom = new KingdomBuilder(game).build();
        IMarket market = new MarketBuilder().build();

        market.addOffer(kingdom, MarketResource.food, 100, 100);
        market.addOffer(kingdom, MarketResource.food, 100, 200);
        var offers = market.getOffersByResource(MarketResource.food);
        assertTrue(!offers.isEmpty());
        var offer = offers.get(0);

        market.buyExistingOffer(offer, kingdom, kingdom, 100);

        assertEquals(1, market.getOffersByResource(MarketResource.food).size());
    }

    @Test
    void offerComparator_withFirstOfferHigher_shouldReturnMoreThanZero()
    {
        var kingdom = new KingdomBuilder(game).build();
        var offer1 = new MarketOffer(Id.generate(), kingdom, MarketResource.food, 100, 2);
        var offer2 = new MarketOffer(Id.generate(), kingdom, MarketResource.food, 100, 1);

        assertTrue(Market.offerComparator(offer1, offer2) > 0);
    }

    @Test
    void offerComparator_withFirstOfferLower_shouldReturnLessThanZero()
    {
        var kingdom = new KingdomBuilder(game).build();
        var offer1 = new MarketOffer(Id.generate(), kingdom, MarketResource.food, 100, 1);
        var offer2 = new MarketOffer(Id.generate(), kingdom, MarketResource.food, 100, 2);

        assertTrue(Market.offerComparator(offer1, offer2) < 0);
    }

    @Test
    void offerComparator_withTheSamePricedOffers_shouldReturnZero()
    {
        var kingdom = new KingdomBuilder(game).build();
        var offer1 = new MarketOffer(Id.generate(), kingdom, MarketResource.food, 100, 1);
        var offer2 = new MarketOffer(Id.generate(), kingdom, MarketResource.food, 100, 1);

        assertEquals(0, Market.offerComparator(offer1, offer2));
    }

    @Test
    void offerComparator_withTheSameOffer_shouldReturnZero()
    {
        var kingdom = new KingdomBuilder(game).build();
        var offer = new MarketOffer(Id.generate(), kingdom, MarketResource.food, 100, 1);

        assertEquals(0, Market.offerComparator(offer, offer));
    }

    @Test
    void findCheapestOffer_withNoOffers_shouldReturnEmpty()
    {
        IMarket market = new MarketBuilder().build();

        var emptyOffer = market.getCheapestOfferByResource(MarketResource.food);

        assertTrue(emptyOffer.isEmpty());
    }

    @Disabled
    @Test
    void findCheapestOffer_withOnlyOneOffer_shouldReturnThatOffer()
    {
        var kingdom = new KingdomBuilder(game).build();
        IMarket market = new MarketBuilder().build();
        market.addOffer(kingdom, MarketResource.food, 100, 1);

        var onlyOffer = market.getCheapestOfferByResource(MarketResource.food);

        assertTrue(onlyOffer.isPresent());
        assertEquals(100, onlyOffer.get().count);
        assertEquals(1, onlyOffer.get().price);
    }

    @Disabled
    @Test
    void findCheapestOffer_withAFewOffers_shouldReturnTheCheapestOne()
    {
        var kingdom = new KingdomBuilder(game).build();
        IMarket market = new MarketBuilder().build();
        market.addOffer(kingdom, MarketResource.food, 100, 2);
        market.addOffer(kingdom, MarketResource.food, 100, 2);
        market.addOffer(kingdom, MarketResource.food, 100, 3);
        market.addOffer(kingdom, MarketResource.food, 100, 4);
        market.addOffer(kingdom, MarketResource.food, 100, 1);

        var cheapestOffer = market.getCheapestOfferByResource(MarketResource.food);

        assertEquals(1, cheapestOffer.get().price);
    }
}
