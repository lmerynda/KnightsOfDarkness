package com.knightsofdarkness.game.simulation;

import java.util.ArrayList;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.game.TestGame;
import com.knightsofdarkness.game.bot.BlacksmithBot;
import com.knightsofdarkness.game.bot.Bot;
import com.knightsofdarkness.game.bot.FarmerBot;
import com.knightsofdarkness.game.bot.GoldMinerBot;
import com.knightsofdarkness.game.bot.IronMinerBot;
import com.knightsofdarkness.game.kingdom.ResourceName;
import com.knightsofdarkness.game.utils.KingdomBuilder;
import com.knightsofdarkness.game.utils.KingdomPrinter;

public class BotGame {
    @Test
    @Disabled
    void simulateTenTurnsTest()
    {
        var game = new TestGame().get();
        var market = game.getMarket();
        var bots = new ArrayList<Bot>();
        var numberOfTurns = 10;
        var kingdomBuilder = Utils.setupKingdomStartConfiguration(new KingdomBuilder(game), game).withResource(ResourceName.turns, numberOfTurns);

        var farmerKingdom = kingdomBuilder.withName("FarmerBot").build();
        Bot farmerBot = new FarmerBot(farmerKingdom, market);
        game.addKingdom(farmerKingdom);
        bots.add(farmerBot);

        var ironMinerKingdom = kingdomBuilder.withName("IronMinerBot").build();
        Bot ironMinerBot = new IronMinerBot(ironMinerKingdom, market);
        game.addKingdom(ironMinerKingdom);
        bots.add(ironMinerBot);

        var blacksmithKingdom = kingdomBuilder.withName("BlacksmithBot").build();
        Bot blacksmithBot = new BlacksmithBot(blacksmithKingdom, market);
        game.addKingdom(blacksmithKingdom);
        bots.add(blacksmithBot);

        var goldMinerKingdom1 = kingdomBuilder.withName("GoldMinerBot1").build();
        Bot goldMinerBot1 = new GoldMinerBot(goldMinerKingdom1, market);
        game.addKingdom(goldMinerKingdom1);
        bots.add(goldMinerBot1);

        var goldMinerKingdom2 = kingdomBuilder.withName("GoldMinerBot2").build();
        Bot goldMinerBot2 = new GoldMinerBot(goldMinerKingdom2, market);
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
