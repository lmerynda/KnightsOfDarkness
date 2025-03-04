package com.knightsofdarkness.web.kingdom.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.common.kingdom.ResourceName;
import com.knightsofdarkness.common.market.MarketResource;
import com.knightsofdarkness.web.Game;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.legacy.TestGame;
import com.knightsofdarkness.web.utils.KingdomBuilder;

class KingdomMarketTest {
    private Game game;
    private GameConfig gameConfig;
    private KingdomBuilder kingdomBuilder;
    private KingdomEntity kingdom;

    @BeforeEach
    void setUp()
    {
        game = new TestGame().get();
        gameConfig = game.getConfig();
        kingdomBuilder = new KingdomBuilder(game);
        kingdom = kingdomBuilder.withRichConfiguration().build();
        game.addKingdom(kingdom);
    }

    @Test
    void whenKingdomHasEnoughGold_reserveGold_shouldReserveAllRequested()
    {
        kingdom.getResources().addCount(ResourceName.gold, 1000);
        var action = new KingdomMarketAction(kingdom);
        int goldReserved = action.reserveGoldForOffer(10, 1);
        assertEquals(10, goldReserved);
    }

    @Test
    void whenKingdomHasNotEnoughGold_reserveGold_shouldReserveNothing()
    {
        kingdom.getResources().setCount(ResourceName.gold, 0);
        var action = new KingdomMarketAction(kingdom);
        int goldReserved = action.reserveGoldForOffer(10, 1);
        assertEquals(0, goldReserved);
    }

    @Test
    void whenKingdomHasNotEnoughGold_reserveGold_shouldNotChangeKingdomGold()
    {
        int initialGold = 50;
        kingdom.getResources().setCount(ResourceName.gold, initialGold);
        var action = new KingdomMarketAction(kingdom);
        action.reserveGoldForOffer(100, 1);
        int kingdomGold = kingdom.getResources().getCount(ResourceName.gold);
        assertEquals(initialGold, kingdomGold);
    }

    @Test
    void whenKingdomHasEnoughGold_reserveGold_shouldChangeKingdomGold()
    {
        int initialGold = 5000;
        int price = 100;
        kingdom.getResources().setCount(ResourceName.gold, initialGold);
        var action = new KingdomMarketAction(kingdom);
        action.reserveGoldForOffer(price, 1);
        int kingdomGold = kingdom.getResources().getCount(ResourceName.gold);
        assertEquals(initialGold - price, kingdomGold);
    }

    @Test
    void whenKingdomBuysMultipleResources_reserveGold_shouldChangeKingdomGold()
    {
        int initialGold = 5000;
        int price = 100;
        int amount = 50;
        kingdom.getResources().setCount(ResourceName.gold, initialGold);
        var action = new KingdomMarketAction(kingdom);
        action.reserveGoldForOffer(price, amount);
        int kingdomGold = kingdom.getResources().getCount(ResourceName.gold);
        assertEquals(initialGold - price * amount, kingdomGold);
    }

    @Test
    void whenKingdomBuysMultipleResourcesAndHasGoldForLimitedAmount_reserveGold_shouldChangeKingdomGold()
    {
        int initialGold = 5000;
        int price = 100;
        int amount = 70;
        kingdom.getResources().setCount(ResourceName.gold, initialGold);
        var action = new KingdomMarketAction(kingdom);
        int reservedGold = action.reserveGoldForOffer(price, amount);
        int kingdomGold = kingdom.getResources().getCount(ResourceName.gold);
        assertThat(reservedGold).isLessThan(amount * price);
        assertThat(kingdomGold).isEqualTo(initialGold - reservedGold);
    }

    @Test
    void whenKingdomDoesNotHaveAnyFood_postMarketOffer_shouldNotPostOfferAndShouldNotRemoveResources()
    {
        kingdom.getResources().setCount(ResourceName.food, 0);
        int initialFood = kingdom.getResources().getCount(ResourceName.food);
        var action = new KingdomMarketAction(kingdom);
        int postedFood = action.postOffer(MarketResource.food, 100);
        int kingdomFood = kingdom.getResources().getCount(ResourceName.food);
        assertEquals(0, postedFood);
        assertEquals(initialFood, kingdomFood);
    }

    @Test
    void whenOfferDoesNotBelongToKingdom_withdrawingIt_shouldNotAddKingdomResourcesBack()
    {
        var foreignKingdom = new KingdomBuilder(game).withName("foreignKingdo").build();
        game.addKingdom(foreignKingdom);
        var market = game.getMarket();
        var result = market.createOffer(foreignKingdom.getName(), MarketResource.food, 100, 50);
        var offer = market.findOfferById(result.data().get().id());
        assertTrue(offer.isPresent());
        kingdom.getResources().setCount(ResourceName.food, 1000);
        var action = new KingdomMarketAction(kingdom);
        action.withdrawMarketOffer(offer.get());
        int kingdomFood = kingdom.getResources().getCount(ResourceName.food);
        assertEquals(1000, kingdomFood);
    }
}
