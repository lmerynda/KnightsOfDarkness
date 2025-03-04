package com.knightsofdarkness.web.kingdom.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.common.kingdom.ResourceName;
import com.knightsofdarkness.common.kingdom.SendCarriersDto;
import com.knightsofdarkness.common.kingdom.UnitName;
import com.knightsofdarkness.common.market.MarketResource;
import com.knightsofdarkness.web.Game;
import com.knightsofdarkness.web.legacy.TestGame;
import com.knightsofdarkness.web.utils.KingdomBuilder;

class KingdomCarriersActionTest {
    private static Game game;
    private KingdomBuilder kingdomBuilder;
    private KingdomEntity kingdom;

    @BeforeAll
    static void beforeAll()
    {
        game = new TestGame().get();
    }

    @BeforeEach
    void setUp()
    {
        kingdomBuilder = new KingdomBuilder(game);
        kingdom = kingdomBuilder.build();
    }

    @Test
    void whenKingdomHasNoCarriers_sendingCarriers_shouldResultInFailure()
    {
        kingdom.getUnits().setCount(UnitName.carrier, 0);
        kingdom.getResources().setCount(ResourceName.food, 100);

        var sendCarriersDto = new SendCarriersDto("destination", MarketResource.food, 100);
        var sendCarriersResult = kingdom.sendCarriers(sendCarriersDto);

        assertFalse(sendCarriersResult.success());
        assertEquals(100, kingdom.getResources().getCount(ResourceName.food));
    }

    @Test
    void whenKingdomHasCarriersButNoResources_sendingCarriers_shouldResultInFailure()
    {
        kingdom.getUnits().setCount(UnitName.carrier, 10);
        kingdom.getResources().setCount(ResourceName.food, 0);

        var sendCarriersDto = new SendCarriersDto("destination", MarketResource.food, 100);
        var sendCarriersResult = kingdom.sendCarriers(sendCarriersDto);

        assertFalse(sendCarriersResult.success());
        assertEquals(10, kingdom.getUnits().getAvailableCount(UnitName.carrier));
    }

    @Test
    void whenKingdomHasCarriersAndResources_sendingCarriers_shouldResultInSuccess()
    {
        kingdom.getUnits().setCount(UnitName.carrier, 10);
        kingdom.getResources().setCount(ResourceName.food, 100);

        var sendCarriersDto = new SendCarriersDto("destination", MarketResource.food, 50);
        var sendCarriersResult = kingdom.sendCarriers(sendCarriersDto);

        assertTrue(sendCarriersResult.success());
        assertEquals(50, kingdom.getResources().getCount(ResourceName.food));
        assertEquals(50, kingdom.getResources().getCount(ResourceName.food));
        assertEquals(9, kingdom.getUnits().getAvailableCount(UnitName.carrier));
    }

    @Test
    void whenKingdomHasCarriersButNotEnoughResources_sendingCarriers_shouldSendOnlyAvailableResources()
    {
        kingdom.getUnits().setCount(UnitName.carrier, 10);
        kingdom.getResources().setCount(ResourceName.food, 50);

        var sendCarriersDto = new SendCarriersDto("destination", MarketResource.food, 100);
        var sendCarriersResult = kingdom.sendCarriers(sendCarriersDto);

        assertTrue(sendCarriersResult.success());
        assertThat(sendCarriersResult.data().get().amount()).isEqualTo(50);
        assertEquals(0, kingdom.getResources().getCount(ResourceName.food));
        assertEquals(9, kingdom.getUnits().getAvailableCount(UnitName.carrier));
    }

    @Test
    void whenKingdomHasCarriersButNotEnoughTotalCapacity_sendingCarriers_shouldSendResourcesUpToCapacity()
    {
        kingdom.getUnits().setCount(UnitName.carrier, 1);
        kingdom.getResources().setCount(ResourceName.food, 1000);
        int carrierCapacity = kingdom.getConfig().carrierCapacity().get(MarketResource.food);

        var sendCarriersDto = new SendCarriersDto("destination", MarketResource.food, 1000);
        var sendCarriersResult = kingdom.sendCarriers(sendCarriersDto);

        assertTrue(sendCarriersResult.success());
        assertThat(sendCarriersResult.data().get().amount()).isEqualTo(carrierCapacity);
        assertEquals(900, kingdom.getResources().getCount(ResourceName.food));
        assertEquals(0, kingdom.getUnits().getAvailableCount(UnitName.carrier));
    }

    @Test
    void whenKingdomHasCarriersAndResources_sendingCarriers_shouldSendCarriersOut()
    {
        kingdom.getUnits().setCount(UnitName.carrier, 10);
        kingdom.getResources().setCount(ResourceName.food, 100);

        var sendCarriersDto = new SendCarriersDto("destination", MarketResource.food, 50);
        var sendCarriersResult = kingdom.sendCarriers(sendCarriersDto);

        assertTrue(sendCarriersResult.success());
        assertEquals(1, kingdom.getCarriersOnTheMove().size());
    }
}
