package com.knightsofdarkness.game.kingdom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.common.kingdom.ResourceName;
import com.knightsofdarkness.common.market.MarketResource;
import com.knightsofdarkness.game.Game;
import com.knightsofdarkness.game.TestGame;
import com.knightsofdarkness.game.utils.KingdomBuilder;

class KingdomMarketTest {
    private static Game game;
    private Kingdom kingdom;

    @BeforeAll
    static void beforeAll()
    {
        game = new TestGame().get();
    }

    @BeforeEach
    void setUp()
    {
        this.kingdom = new KingdomBuilder(game).build();
    }

    @Test
    void whenKingdomHasEnoughGold_reserveGold_shouldReserveAllRequested()
    {
        kingdom.getResources().addCount(ResourceName.gold, 1000);
        int goldReserved = kingdom.reserveGoldForOffer(10, 1);
        assertEquals(10, goldReserved);
    }

    @Test
    void whenKingdomHasNotEnoughGold_reserveGold_shouldReserveNothing()
    {
        kingdom.getResources().setCount(ResourceName.gold, 0);
        int goldReserved = kingdom.reserveGoldForOffer(10, 1);
        assertEquals(0, goldReserved);
    }

    @Test
    void whenKingdomHasNotEnoughGold_reserveGold_shouldNotChangeKingdomGold()
    {
        int initialGold = 50;
        kingdom.getResources().setCount(ResourceName.gold, initialGold);
        kingdom.reserveGoldForOffer(100, 1);
        int kingdomGold = kingdom.getResources().getCount(ResourceName.gold);
        assertEquals(initialGold, kingdomGold);
    }

    @Test
    void whenKingdomHasEnoughGold_reserveGold_shouldChangeKingdomGold()
    {
        int initialGold = 5000;
        int price = 100;
        kingdom.getResources().setCount(ResourceName.gold, initialGold);
        kingdom.reserveGoldForOffer(price, 1);
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
        kingdom.reserveGoldForOffer(price, amount);
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
        int reservedGold = kingdom.reserveGoldForOffer(price, amount);
        int kingdomGold = kingdom.getResources().getCount(ResourceName.gold);
        assertThat(reservedGold).isLessThan(amount * price);
        assertThat(kingdomGold).isEqualTo(initialGold - reservedGold);
    }

    @Test
    void whenKingdomDoesNotHaveAnyFood_postMarketOffer_shouldNotPostOfferAndShouldNotRemoveResources()
    {
        kingdom.getResources().setCount(ResourceName.food, 0);
        int initialFood = kingdom.getResources().getCount(ResourceName.food);
        int postedFood = kingdom.postMarketOffer(MarketResource.food, 100);
        int kingdomFood = kingdom.getResources().getCount(ResourceName.food);
        assertEquals(0, postedFood);
        assertEquals(initialFood, kingdomFood);
    }

    @Disabled("Fix when return types are settled")
    @Test
    void whenOfferDoesNotBelongToKingdom_withdrawingIt_shouldNotAddKingdomResourcesBack()
    {
        var foreignKingdom = new KingdomBuilder(game).withName("foreignKingdo").build();
        game.addKingdom(foreignKingdom);
        var market = game.getMarket();
        var offer = market.createOffer(foreignKingdom.getName(), MarketResource.food, 100, 50).data().get();
        kingdom.getResources().setCount(ResourceName.food, 1000);
        // kingdom.withdrawMarketOffer(offer);
        int kingdomFood = kingdom.getResources().getCount(ResourceName.food);
        assertEquals(1000, kingdomFood);
    }
}
