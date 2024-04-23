package com.knightsofdarkness.game.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.knightsofdarkness.game.TestGame;
import com.knightsofdarkness.game.bot.GoldMinerBot;
import com.knightsofdarkness.game.kingdom.ResourceName;
import com.knightsofdarkness.game.market.MarketResource;
import com.knightsofdarkness.game.utils.KingdomBuilder;
import com.knightsofdarkness.game.utils.KingdomPrinter;

public class SingleGoldMinerGame {
    @Test
    void simulateTenTurnsTest()
    {
        var game = new TestGame().get();

        var kingdomBuilder = new KingdomBuilder(game);
        kingdomBuilder = Utils.setupKingdomStartConfiguration(kingdomBuilder, game);
        var goldMinerKingdom = kingdomBuilder.withName("GoldMinerBot").withResource(ResourceName.turns, 10).build();
        var goldMinerBot = new GoldMinerBot(goldMinerKingdom);
        game.addKingdom(goldMinerBot.getKingdom());

        var infiniteFarmerKingdom = kingdomBuilder.withName("InfiniteFarmer").withResource(ResourceName.food, 1000000).build();
        game.getMarket().addOffer(infiniteFarmerKingdom, MarketResource.food, 1000000, 5);

        KingdomPrinter.printResourcesHeader();
        KingdomPrinter.printLineSeparator();

        KingdomPrinter.kingdomInfoPrinter(goldMinerBot.getKingdom());
        KingdomPrinter.printLineSeparator();

        for (var i = 0; i < 10; i++)
        {
            goldMinerBot.doAllActions();
            goldMinerBot.passTurn();
            KingdomPrinter.kingdomInfoPrinter(goldMinerBot.getKingdom());
            KingdomPrinter.printLineSeparator();
        }

        assertEquals(0, goldMinerBot.getKingdom().getResources().getCount(ResourceName.turns));
    }
}
