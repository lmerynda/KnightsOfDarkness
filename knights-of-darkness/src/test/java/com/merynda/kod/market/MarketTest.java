package com.merynda.kod.market;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.merynda.kod.TestGame;
import com.merynda.kod.game.Game;
import com.merynda.kod.market.Market;
import com.merynda.kod.market.MarketOffer;
import com.merynda.kod.market.MarketResource;
import com.merynda.kod.utils.KingdomBuilder;

class MarketTest {
    private static Game game;

    @BeforeAll
    static void beforeAll()
    {
        game = new TestGame().get();
    }

    @Test
    void testAddOffer()
    {
        var kingdom = new KingdomBuilder(game).build();
        var market = new Market();

        market.addOffer(kingdom, MarketResource.food, 100, 50);

        assertEquals(1, market.getOffersByResource(MarketResource.food).size());
    }

    @Test
    void testThreeOffers()
    {
        var kingdom = new KingdomBuilder(game).build();
        var market = new Market();

        market.addOffer(kingdom, MarketResource.food, 100, 50);
        market.addOffer(kingdom, MarketResource.food, 100, 50);
        market.addOffer(kingdom, MarketResource.food, 100, 50);
        market.addOffer(kingdom, MarketResource.food, 100, 51);
        market.addOffer(kingdom, MarketResource.food, 100, 52);

        assertEquals(5, market.getOffersByResource(MarketResource.food).size());
    }

    @Test
    void testRemoveOffer()
    {
        var kingdom = new KingdomBuilder(game).build();
        var market = new Market();
        var offer = market.addOffer(kingdom, MarketResource.food, 100, 50);

        market.removeOffer(offer);

        assertEquals(0, market.getOffersByResource(MarketResource.food).size());
    }

    @Test
    void testRemoveThreeOffers()
    {
        var kingdom = new KingdomBuilder(game).build();
        var market = new Market();
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
    void buyingOffer_whenNoOffersExist_shouldNotBuyAnything()
    {
        var kingdom = new KingdomBuilder(game).build();
        var market = new Market();

        var offer = new MarketOffer(kingdom, MarketResource.food, 100, 100);

        var amountBought = market.buyExistingOffer(offer, 100);

        assertEquals(0, market.getOffersByResource(MarketResource.food).size());
        assertEquals(0, amountBought);
    }

    @Test
    void buyingOffer_whenOneOfferExistsAndHasEnoughAmount_shouldSellEntireRequestedAmountAndStillHasOfferAvailable()
    {
        var kingdom = new KingdomBuilder(game).build();
        var market = new Market();

        market.addOffer(kingdom, MarketResource.food, 111, 100);
        var offers = market.getOffersByResource(MarketResource.food);
        assertTrue(!offers.isEmpty());

        var amountBought = market.buyExistingOffer(offers.get(0), 100);

        assertEquals(1, market.getOffersByResource(MarketResource.food).size());
        assertEquals(100, amountBought);
    }

    @Test
    void buyingOffer_whenOneOfferExistsAndHasExactlyTheSameAmount_shouldSellEntireRequestedAmountAndHasNoOffersAvailable()
    {
        var kingdom = new KingdomBuilder(game).build();
        var market = new Market();

        market.addOffer(kingdom, MarketResource.food, 100, 100);
        var offers = market.getOffersByResource(MarketResource.food);
        assertTrue(!offers.isEmpty());

        var amountBought = market.buyExistingOffer(offers.get(0), 100);

        assertEquals(0, market.getOffersByResource(MarketResource.food).size());
        assertEquals(100, amountBought);
    }

    @Test
    void buyingOffer_whenOfferExist_shouldReduceTheAmountStillAvailableByTheAmountBought()
    {
        var kingdom = new KingdomBuilder(game).build();
        var market = new Market();

        market.addOffer(kingdom, MarketResource.food, 100, 100);
        var offers = market.getOffersByResource(MarketResource.food);
        assertTrue(!offers.isEmpty());
        var offer = offers.get(0);

        var amountBought = market.buyExistingOffer(offer, 20);

        assertEquals(1, market.getOffersByResource(MarketResource.food).size());
        assertEquals(100 - amountBought, offer.getCount());
    }

    @Test
    void buyingOffer_whenMultipleOffersExistAndBuyersTakeTheEntireOne_shouldReduceNumberOfAvailableOffersByOne()
    {
        var kingdom = new KingdomBuilder(game).build();
        var market = new Market();

        market.addOffer(kingdom, MarketResource.food, 100, 100);
        market.addOffer(kingdom, MarketResource.food, 100, 200);
        var offers = market.getOffersByResource(MarketResource.food);
        assertTrue(!offers.isEmpty());
        var offer = offers.get(0);

        market.buyExistingOffer(offer, 100);

        assertEquals(1, market.getOffersByResource(MarketResource.food).size());
    }

    @Test
    void offerComparator_withFirstOfferHigher_shouldReturnMoreThanZero()
    {
        var kingdom = new KingdomBuilder(game).build();
        var offer1 = new MarketOffer(kingdom, MarketResource.food, 100, 2);
        var offer2 = new MarketOffer(kingdom, MarketResource.food, 100, 1);

        assertTrue(Market.offerComparator(offer1, offer2) > 0);
    }

    @Test
    void offerComparator_withFirstOfferLower_shouldReturnLessThanZero()
    {
        var kingdom = new KingdomBuilder(game).build();
        var offer1 = new MarketOffer(kingdom, MarketResource.food, 100, 1);
        var offer2 = new MarketOffer(kingdom, MarketResource.food, 100, 2);

        assertTrue(Market.offerComparator(offer1, offer2) < 0);
    }

    @Test
    void offerComparator_withTheSamePricedOffers_shouldReturnZero()
    {
        var kingdom = new KingdomBuilder(game).build();
        var offer1 = new MarketOffer(kingdom, MarketResource.food, 100, 1);
        var offer2 = new MarketOffer(kingdom, MarketResource.food, 100, 1);

        assertEquals(0, Market.offerComparator(offer1, offer2));
    }

    @Test
    void offerComparator_withTheSameOffer_shouldReturnZero()
    {
        var kingdom = new KingdomBuilder(game).build();
        var offer = new MarketOffer(kingdom, MarketResource.food, 100, 1);

        assertEquals(0, Market.offerComparator(offer, offer));
    }

    @Test
    void findCheapestOffer_withNoOffers_shouldReturnEmpty()
    {
        var market = new Market();

        var emptyOffer = market.getCheapestOfferByResource(MarketResource.food);

        assertTrue(emptyOffer.isEmpty());
    }

    @Test
    void findCheapestOffer_withOnlyOneOffer_shouldReturnThatOffer()
    {
        var kingdom = new KingdomBuilder(game).build();
        var market = new Market();
        market.addOffer(kingdom, MarketResource.food, 100, 1);

        var onlyOffer = market.getCheapestOfferByResource(MarketResource.food);

        assertTrue(onlyOffer.isPresent());
        assertEquals(100, onlyOffer.get().count);
        assertEquals(1, onlyOffer.get().price);
    }

    @Test
    void findCheapestOffer_withAFewOffers_shouldReturnTheCheapestOne()
    {
        var kingdom = new KingdomBuilder(game).build();
        var market = new Market();
        market.addOffer(kingdom, MarketResource.food, 100, 2);
        market.addOffer(kingdom, MarketResource.food, 100, 2);
        market.addOffer(kingdom, MarketResource.food, 100, 3);
        market.addOffer(kingdom, MarketResource.food, 100, 4);
        market.addOffer(kingdom, MarketResource.food, 100, 1);

        var cheapestOffer = market.getCheapestOfferByResource(MarketResource.food);

        assertEquals(1, cheapestOffer.get().price);
    }
}