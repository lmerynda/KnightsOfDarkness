package com.merynda.kod.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.merynda.kod.TestGame;
import com.merynda.kod.bot.GoldMinerBot;
import com.merynda.kod.kingdom.ResourceName;
import com.merynda.kod.market.MarketResource;
import com.merynda.kod.utils.KingdomBuilder;
import com.merynda.kod.utils.KingdomPrinter;

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
