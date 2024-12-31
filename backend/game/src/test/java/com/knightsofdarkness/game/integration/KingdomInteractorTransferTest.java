package com.knightsofdarkness.game.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.common.kingdom.ResourceName;
import com.knightsofdarkness.common.kingdom.SendCarriersDto;
import com.knightsofdarkness.common.kingdom.UnitName;
import com.knightsofdarkness.common.market.MarketResource;
import com.knightsofdarkness.game.Game;
import com.knightsofdarkness.game.TestGame;
import com.knightsofdarkness.game.interactions.IKingdomInteractor;
import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.game.utils.KingdomBuilder;

class KingdomInteractorTransferTest {
    private Game game;
    private IKingdomInteractor kingdomInteractor;
    private Kingdom primaryKingdom;
    private Kingdom secondaryKingdom;
    private static final int weaponsProductionPercentage = 0;

    @BeforeEach
    void setUp()
    {
        game = new TestGame().get();
        kingdomInteractor = game.getKingdomInteractor();
        primaryKingdom = new KingdomBuilder(game).withName("primary").build();
        game.addKingdom(primaryKingdom);
        secondaryKingdom = new KingdomBuilder(game).withName("secondary").build();
        game.addKingdom(secondaryKingdom);
    }

    @Test
    void whenPrimaryKingdomTransfersResources_thenSecondaryKingdomReceiveThem()
    {
        secondaryKingdom.getResources().setCount(ResourceName.food, 0);
        kingdomInteractor.transferResources(primaryKingdom, secondaryKingdom.getName(), MarketResource.food, 100);
        assertEquals(100, secondaryKingdom.getResources().getCount(ResourceName.food));
    }

    @Test
    void whenPrimaryKingdomSendsCarriersAndCompletesTransfer_thenSecondaryKingdomReceiveResources()
    {
        secondaryKingdom.getResources().setCount(ResourceName.food, 0);

        primaryKingdom.getResources().setCount(ResourceName.food, 1000);
        primaryKingdom.getUnits().setCount(UnitName.carrier, 100);
        var data = new SendCarriersDto(secondaryKingdom.getName(), MarketResource.food, 100);
        var result = primaryKingdom.sendCarriers(data);
        assertTrue(result.success());

        var numberOfTurns = game.getConfig().common().turnsToDeliverResources();
        for (int i = 0; i < numberOfTurns; i++)
        {
            primaryKingdom.passTurn(kingdomInteractor, weaponsProductionPercentage);
        }
        assertEquals(0, primaryKingdom.getCarriersOnTheMove().size());
        assertEquals(100, secondaryKingdom.getResources().getCount(ResourceName.food));
    }
}
