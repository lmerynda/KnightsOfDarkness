package com.knightsofdarkness.web.bots;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.web.Game;
import com.knightsofdarkness.web.common.kingdom.BuildingName;
import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.market.MarketResource;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;
import com.knightsofdarkness.web.legacy.TestGame;
import com.knightsofdarkness.web.utils.KingdomBuilder;

class BotLimitsTest {
    private Game game;
    private GameConfig gameConfig;
    private KingdomEntity kingdom;
    private FarmerBot bot;

    @BeforeEach
    void setUp()
    {
        game = new TestGame().get();
        gameConfig = game.getConfig();
        kingdom = new KingdomBuilder(game).withResource(ResourceName.gold, 100000000).build();
        game.addKingdom(kingdom);
        bot = new FarmerBot(kingdom, game.getMarket(), game.getKingdomInteractor(), gameConfig);
    }

    private void fillAllLand(int land)
    {
        kingdom.getResources().setCount(ResourceName.land, land);
        kingdom.getBuildings().addCount(BuildingName.house, land - kingdom.getBuildings().countAll());
    }

    @Test
    void bot_shouldBuyLandOnlyUpToTheCap()
    {
        var maxLand = gameConfig.bots().maxLand();
        fillAllLand(maxLand - 1);

        assertEquals(1, bot.botFunctions.buyLandToMaintainUnused(kingdom, 2));
        assertEquals(maxLand, kingdom.getResources().getCount(ResourceName.land));

        fillAllLand(maxLand);
        assertEquals(0, bot.botFunctions.buyLandToMaintainUnused(kingdom, 2));
        assertEquals(maxLand, kingdom.getResources().getCount(ResourceName.land));
    }

    @Test
    void bot_shouldOfferAtMostTheCapAtOnce()
    {
        kingdom.getResources().setCount(ResourceName.food, 10000000);

        bot.botFunctions.postOffer(kingdom, game.getMarket(), MarketResource.food, 10000000, 15);

        var offers = game.getMarket().getOffersByKingdomName(kingdom.getName());
        assertEquals(1, offers.size());
        assertEquals(gameConfig.bots().getMaxOffer(MarketResource.food), offers.get(0).getCount());
    }

    @Test
    void bot_shouldShipStoresAboveTheCapAway()
    {
        kingdom.getResources().setCount(ResourceName.food, 10000000);
        kingdom.getResources().setCount(ResourceName.iron, 5);

        BotFunctions.shipSurplusAway(kingdom, gameConfig);

        assertEquals(gameConfig.bots().getMaxStock(ResourceName.food), kingdom.getResources().getCount(ResourceName.food));
        assertEquals(5, kingdom.getResources().getCount(ResourceName.iron));
    }
}
