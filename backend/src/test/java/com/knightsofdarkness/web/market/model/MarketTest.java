package com.knightsofdarkness.web.market.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.web.Game;
import com.knightsofdarkness.web.common.market.MarketResource;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;
import com.knightsofdarkness.web.legacy.TestGame;
import com.knightsofdarkness.web.market.IMarket;
import com.knightsofdarkness.web.utils.Id;
import com.knightsofdarkness.web.utils.KingdomBuilder;

class MarketTest {
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
    void testAddOffer()
    {
        market.createOffer(kingdom.getName(), MarketResource.food, 1, 50);

        assertEquals(1, market.getOffersByResource(MarketResource.food).size());
    }

    @Test
    void testAddThreeOffers()
    {
        market.createOffer(kingdom.getName(), MarketResource.food, 1, 50);
        market.createOffer(kingdom.getName(), MarketResource.food, 1, 60);
        market.createOffer(kingdom.getName(), MarketResource.food, 1, 5);

        assertEquals(3, market.getOffersByResource(MarketResource.food).size());
    }

    @Test
    void testRemoveOffer()
    {
        var offer = market.createOffer(kingdom.getName(), MarketResource.food, 100, 50).data().get();
        assertEquals(1, market.getOffersByResource(MarketResource.food).size());

        market.removeOffer(offer.id());

        assertEquals(0, market.getOffersByResource(MarketResource.food).size());
    }

    @Test
    void testRemoveThreeOffers()
    {
        var offer1 = market.createOffer(kingdom.getName(), MarketResource.food, 100, 50).data().get();
        var offer2 = market.createOffer(kingdom.getName(), MarketResource.food, 100, 60).data().get();
        var offer3 = market.createOffer(kingdom.getName(), MarketResource.food, 100, 5).data().get();
        assertEquals(3, market.getOffersByResource(MarketResource.food).size());

        market.removeOffer(offer1.id());
        market.removeOffer(offer2.id());
        market.removeOffer(offer3.id());

        assertEquals(0, market.getOffersByResource(MarketResource.food).size());
    }

    @Test
    void createOffer_withPriceLowerThanMinimum_shouldFail()
    {
        int minPrice = game.getConfig().market().minPrice();
        int lowerThanMinPrice = minPrice - 1;

        var result = market.createOffer(kingdom.getName(), MarketResource.food, 100, lowerThanMinPrice);

        assertFalse(result.success());
        assertTrue(result.data().isEmpty());
    }

    @Test
    void createOffer_withNonExistingKingdom_shouldFail()
    {
        String nonExistingKingdomName = "NonExistingKingdom";

        var result = market.createOffer(nonExistingKingdomName, MarketResource.food, 100, 50);

        assertFalse(result.success());
        assertTrue(result.data().isEmpty());
    }

    @Test
    void whenMaxOffersForResourceIsReached_newOfferShouldNotBeCreated()
    {
        for (int i = 0; i < game.getConfig().market().maxKingdomOffers(); i++)
        {
            market.createOffer(kingdom.getName(), MarketResource.food, 100, 50);
        }

        var result = market.createOffer(kingdom.getName(), MarketResource.food, 100, 50);
        assertFalse(result.success());
        assertTrue(result.data().isEmpty());
    }

    @Test
    void buyingOffer_whenOneOfferExistsAndHasEnoughAmount_shouldSellEntireRequestedAmountAndStillHasOfferAvailable()
    {
        market.createOffer(kingdom.getName(), MarketResource.food, 100, 50);
        var maybeOffer = market.getCheapestOfferByResource(MarketResource.food);
        assertTrue(maybeOffer.isPresent());
        var offer = maybeOffer.get();

        market.buyExistingOffer(offer, kingdom, kingdom, 99);

        var cheapestOffer = market.getCheapestOfferByResource(MarketResource.food);
        assertTrue(cheapestOffer.isPresent());

        assertEquals(offer.getId(), cheapestOffer.get().getId());
    }

    @Test
    void buyingOffer_whenOneOfferExistsAndHasExactlyTheSameAmount_shouldSellEntireRequestedAmountAndHasNoOffersAvailable()
    {
        market.createOffer(kingdom.getName(), MarketResource.food, 100, 50);
        var maybeOffer = market.getCheapestOfferByResource(MarketResource.food);
        assertTrue(maybeOffer.isPresent());
        var offer = maybeOffer.get();

        market.buyExistingOffer(offer, kingdom, kingdom, 100);

        var cheapestOffer = market.getCheapestOfferByResource(MarketResource.food);
        assertTrue(cheapestOffer.isEmpty());
    }

    @Test
    void buyingOffer_whenOfferExist_shouldReduceTheAmountStillAvailableByTheAmountBought()
    {
        market.createOffer(kingdom.getName(), MarketResource.food, 100, 50);
        var maybeOffer = market.getCheapestOfferByResource(MarketResource.food);
        assertTrue(maybeOffer.isPresent());
        var offer = maybeOffer.get();

        market.buyExistingOffer(offer, kingdom, kingdom, 90);

        var cheapestOffer = market.getCheapestOfferByResource(MarketResource.food);
        assertTrue(cheapestOffer.isPresent());
        assertEquals(offer.getId(), cheapestOffer.get().getId());
        assertEquals(10, cheapestOffer.get().count);
    }

    @Test
    void buyingOffer_whenMultipleOffersExistAndBuyersTakeTheEntireOne_shouldReduceNumberOfAvailableOffersByOne()
    {
        market.createOffer(kingdom.getName(), MarketResource.food, 100, 50);
        market.createOffer(kingdom.getName(), MarketResource.food, 100, 60);
        market.createOffer(kingdom.getName(), MarketResource.food, 100, 70);
        var maybeOffer = market.getCheapestOfferByResource(MarketResource.food);
        assertTrue(maybeOffer.isPresent());
        var offer = maybeOffer.get();

        market.buyExistingOffer(offer, kingdom, kingdom, 100);

        assertEquals(2, market.getOffersByResource(MarketResource.food).size());
    }

    @Test
    void offerComparator_withFirstOfferHigher_shouldReturnMoreThanZero()
    {
        var offer1 = new MarketOfferEntity(Id.generate(), kingdom, MarketResource.food, 100, 2);
        var offer2 = new MarketOfferEntity(Id.generate(), kingdom, MarketResource.food, 100, 1);

        assertTrue(Market.offerComparator(offer1, offer2) > 0);
    }

    @Test
    void offerComparator_withFirstOfferLower_shouldReturnLessThanZero()
    {
        var offer1 = new MarketOfferEntity(Id.generate(), kingdom, MarketResource.food, 100, 1);
        var offer2 = new MarketOfferEntity(Id.generate(), kingdom, MarketResource.food, 100, 2);

        assertTrue(Market.offerComparator(offer1, offer2) < 0);
    }

    @Test
    void offerComparator_withTheSamePricedOffers_shouldReturnZero()
    {
        var offer1 = new MarketOfferEntity(Id.generate(), kingdom, MarketResource.food, 100, 1);
        var offer2 = new MarketOfferEntity(Id.generate(), kingdom, MarketResource.food, 100, 1);

        assertEquals(0, Market.offerComparator(offer1, offer2));
    }

    @Test
    void offerComparator_withTheSameOffer_shouldReturnZero()
    {
        var offer = new MarketOfferEntity(Id.generate(), kingdom, MarketResource.food, 100, 1);

        assertEquals(0, Market.offerComparator(offer, offer));
    }

    @Test
    void findCheapestOffer_withNoOffers_shouldReturnEmpty()
    {
        var emptyOffer = market.getCheapestOfferByResource(MarketResource.food);

        assertTrue(emptyOffer.isEmpty());
    }

    @Test
    void findCheapestOffer_withAFewOffers_shouldReturnTheCheapestOne()
    {
        market.createOffer(kingdom.getName(), MarketResource.food, 100, 6);
        market.createOffer(kingdom.getName(), MarketResource.food, 100, 7);
        market.createOffer(kingdom.getName(), MarketResource.food, 100, 8);
        market.createOffer(kingdom.getName(), MarketResource.food, 100, 9);
        var offer = market.createOffer(kingdom.getName(), MarketResource.food, 100, 5).data().get();

        var cheapestOffer = market.getCheapestOfferByResource(MarketResource.food);

        assertEquals(offer.id(), cheapestOffer.get().getId());
    }
}
