package com.knightsofdarkness.web.kingdom.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.web.Game;
import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.kingdom.SendCarriersDto;
import com.knightsofdarkness.web.common.kingdom.UnitName;
import com.knightsofdarkness.web.common.market.MarketResource;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.legacy.TestGame;
import com.knightsofdarkness.web.utils.KingdomBuilder;

class KingdomCarriersActionTest {
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
    void whenKingdomHasNoCarriers_sendingCarriers_shouldResultInFailure()
    {
        kingdom.getUnits().setCount(UnitName.carrier, 0);
        kingdom.getResources().setCount(ResourceName.food, 100);

        var sendCarriersDto = new SendCarriersDto("destination", MarketResource.food, 100);
        var action = new KingdomCarriersAction(kingdom, gameConfig);
        var sendCarriersResult = action.sendCarriers(sendCarriersDto);

        assertFalse(sendCarriersResult.success());
        assertEquals(100, kingdom.getResources().getCount(ResourceName.food));
    }

    @Test
    void whenKingdomHasCarriersButNoResources_sendingCarriers_shouldResultInFailure()
    {
        kingdom.getUnits().setCount(UnitName.carrier, 10);
        kingdom.getResources().setCount(ResourceName.food, 0);

        var sendCarriersDto = new SendCarriersDto("destination", MarketResource.food, 100);
        var action = new KingdomCarriersAction(kingdom, gameConfig);
        var sendCarriersResult = action.sendCarriers(sendCarriersDto);

        assertFalse(sendCarriersResult.success());
        assertEquals(10, kingdom.getUnits().getAvailableCount(UnitName.carrier));
    }

    @Test
    void whenKingdomHasCarriersAndResources_sendingCarriers_shouldResultInSuccess()
    {
        kingdom.getUnits().setCount(UnitName.carrier, 10);
        kingdom.getResources().setCount(ResourceName.food, 100);

        var sendCarriersDto = new SendCarriersDto("destination", MarketResource.food, 50);
        var action = new KingdomCarriersAction(kingdom, gameConfig);
        var sendCarriersResult = action.sendCarriers(sendCarriersDto);

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
        var action = new KingdomCarriersAction(kingdom, gameConfig);
        var sendCarriersResult = action.sendCarriers(sendCarriersDto);

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
        int carrierCapacity = gameConfig.carrierCapacity().get(MarketResource.food);

        var sendCarriersDto = new SendCarriersDto("destination", MarketResource.food, 1000);
        var action = new KingdomCarriersAction(kingdom, gameConfig);
        var sendCarriersResult = action.sendCarriers(sendCarriersDto);

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
        var action = new KingdomCarriersAction(kingdom, gameConfig);
        var sendCarriersResult = action.sendCarriers(sendCarriersDto);

        assertTrue(sendCarriersResult.success());
        assertEquals(1, kingdom.getCarriersOnTheMove().size());
    }
}
