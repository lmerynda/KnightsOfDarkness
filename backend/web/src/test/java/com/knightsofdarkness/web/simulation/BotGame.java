package com.knightsofdarkness.web.simulation;

import java.util.ArrayList;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.common.kingdom.ResourceName;
import com.knightsofdarkness.web.bots.BlacksmithBot;
import com.knightsofdarkness.web.bots.FarmerBot;
import com.knightsofdarkness.web.bots.GoldMinerBot;
import com.knightsofdarkness.web.bots.IBot;
import com.knightsofdarkness.web.bots.IronMinerBot;
import com.knightsofdarkness.web.legacy.TestGame;
import com.knightsofdarkness.web.utils.KingdomBuilder;
import com.knightsofdarkness.web.utils.KingdomPrinter;

class BotGame {
    @Test
    @Disabled
    void simulateTenTurnsTest()
    {
        var game = new TestGame().get();
        var gameConfig = game.getConfig();
        var market = game.getMarket();
        var kingdomInteractor = game.getKingdomInteractor();
        var bots = new ArrayList<IBot>();
        var numberOfTurns = 10;
        var kingdomBuilder = Utils.setupKingdomStartConfiguration(new KingdomBuilder(game), game).withResource(ResourceName.turns, numberOfTurns);

        var farmerKingdom = kingdomBuilder.withName("FarmerBot").build();
        IBot farmerBot = new FarmerBot(farmerKingdom, market, kingdomInteractor, gameConfig);
        game.addKingdom(farmerKingdom);
        bots.add(farmerBot);

        var ironMinerKingdom = kingdomBuilder.withName("IronMinerBot").build();
        IBot ironMinerBot = new IronMinerBot(ironMinerKingdom, market, kingdomInteractor, gameConfig);
        game.addKingdom(ironMinerKingdom);
        bots.add(ironMinerBot);

        var blacksmithKingdom = kingdomBuilder.withName("BlacksmithBot").build();
        IBot blacksmithBot = new BlacksmithBot(blacksmithKingdom, market, kingdomInteractor, gameConfig);
        game.addKingdom(blacksmithKingdom);
        bots.add(blacksmithBot);

        var goldMinerKingdom1 = kingdomBuilder.withName("GoldMinerBot1").build();
        IBot goldMinerBot1 = new GoldMinerBot(goldMinerKingdom1, market, kingdomInteractor, gameConfig);
        game.addKingdom(goldMinerKingdom1);
        bots.add(goldMinerBot1);

        var goldMinerKingdom2 = kingdomBuilder.withName("GoldMinerBot2").build();
        IBot goldMinerBot2 = new GoldMinerBot(goldMinerKingdom2, market, kingdomInteractor);
        game.addKingdom(goldMinerKingdom2);
        bots.add(goldMinerBot2);

        KingdomPrinter.printResourcesHeader();
        KingdomPrinter.printLineSeparator();
        for (var bot : bots)
        {
            KingdomPrinter.kingdomInfoPrinter(bot.getKingdom());
        }
        KingdomPrinter.printLineSeparator();

        for (var currentTurn = 0; currentTurn < numberOfTurns; currentTurn++)
        {
            for (var bot : bots)
            {
                KingdomPrinter.kingdomInfoPrinter(bot.getKingdom());
            }
            KingdomPrinter.printLineSeparator();

            boolean hasAnythingHappened = false;
            do
            {
                hasAnythingHappened = false;
                for (var bot : bots)
                {
                    var result = bot.doActionCycle();
                    hasAnythingHappened = hasAnythingHappened || result;
                }
            } while (hasAnythingHappened);

            for (var bot : bots)
            {
                bot.passTurn();
            }
            KingdomPrinter.printLineSeparator();
            KingdomPrinter.printTurnInfo(currentTurn);
            KingdomPrinter.printLineSeparator();
        }

        for (var bot : bots)
        {
            KingdomPrinter.kingdomInfoPrinter(bot.getKingdom());
        }
        KingdomPrinter.printLineSeparator();

        KingdomPrinter.printResourcesHeader();
    }
}
