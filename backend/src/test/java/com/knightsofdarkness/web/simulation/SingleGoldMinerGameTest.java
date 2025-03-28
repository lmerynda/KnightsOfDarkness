package com.knightsofdarkness.web.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.web.bots.GoldMinerBot;
import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.market.MarketResource;
import com.knightsofdarkness.web.legacy.TestGame;
import com.knightsofdarkness.web.utils.KingdomBuilder;
import com.knightsofdarkness.web.utils.KingdomPrinter;

class SingleGoldMinerGameTest {
    @Disabled
    @Test
    void simulateTenTurnsTest()
    {
        var game = new TestGame().get();
        var gameConfig = game.getConfig();

        var kingdomBuilder = new KingdomBuilder(game);
        kingdomBuilder = Utils.setupKingdomStartConfiguration(kingdomBuilder, game);
        var goldMinerKingdom = kingdomBuilder.withName("GoldMinerBot").withResource(ResourceName.turns, 10).build();
        var goldMinerBot = new GoldMinerBot(goldMinerKingdom, game.getMarket(), game.getKingdomInteractor(), null);
        game.addKingdom(goldMinerBot.getKingdom());

        var infiniteFarmerKingdom = kingdomBuilder.withName("InfiniteFarmer").withResource(ResourceName.food, 1000000).build();
        game.getMarket().createOffer(infiniteFarmerKingdom.getName(), MarketResource.food, 1000000, 5);

        KingdomPrinter.printResourcesHeader();
        KingdomPrinter.printLineSeparator();

        KingdomPrinter.kingdomInfoPrinter(goldMinerBot.getKingdom(), gameConfig);
        KingdomPrinter.printLineSeparator();

        for (var i = 0; i < 10; i++)
        {
            goldMinerBot.doActionCycle();
            goldMinerBot.passTurn();
            KingdomPrinter.kingdomInfoPrinter(goldMinerBot.getKingdom(), gameConfig);
            KingdomPrinter.printLineSeparator();
        }

        assertEquals(0, goldMinerBot.getKingdom().getResources().getCount(ResourceName.turns));
    }
}
