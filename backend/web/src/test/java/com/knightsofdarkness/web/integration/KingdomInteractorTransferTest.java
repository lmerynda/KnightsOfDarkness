package com.knightsofdarkness.web.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.common.kingdom.ResourceName;
import com.knightsofdarkness.common.kingdom.SendCarriersDto;
import com.knightsofdarkness.common.kingdom.UnitName;
import com.knightsofdarkness.common.market.MarketResource;
import com.knightsofdarkness.web.Game;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.kingdom.IKingdomInteractor;
import com.knightsofdarkness.web.kingdom.model.KingdomCarriersAction;
import com.knightsofdarkness.web.kingdom.model.KingdomDetailsProvider;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;
import com.knightsofdarkness.web.kingdom.model.KingdomTurnAction;
import com.knightsofdarkness.web.legacy.TestGame;
import com.knightsofdarkness.web.utils.KingdomBuilder;

class KingdomInteractorTransferTest {
    private Game game;
    private GameConfig gameConfig;
    private IKingdomInteractor kingdomInteractor;
    private KingdomDetailsProvider kingdomDetailsProvider;
    private KingdomEntity primaryKingdom;
    private KingdomEntity secondaryKingdom;
    private static final int weaponsProductionPercentage = 0;

    @BeforeEach
    void setUp()
    {
        game = new TestGame().get();
        gameConfig = game.getConfig();
        kingdomInteractor = game.getKingdomInteractor();
        kingdomDetailsProvider = new KingdomDetailsProvider(gameConfig);
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
        var action = new KingdomCarriersAction(primaryKingdom, gameConfig);
        var result = action.sendCarriers(data);
        assertTrue(result.success());

        var numberOfTurns = game.getConfig().common().turnsToDeliverResources();
        for (int i = 0; i < numberOfTurns; i++)
        {
            var turnAction = new KingdomTurnAction(primaryKingdom, kingdomInteractor, gameConfig, kingdomDetailsProvider);
            turnAction.passTurn(weaponsProductionPercentage);
        }
        assertEquals(0, primaryKingdom.getCarriersOnTheMove().size());
        assertEquals(100, secondaryKingdom.getResources().getCount(ResourceName.food));
    }
}
