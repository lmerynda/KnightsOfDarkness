package com.uprzejmy.kod.simulation;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.uprzejmy.kod.TestGame;
import com.uprzejmy.kod.bot.BlacksmithBot;
import com.uprzejmy.kod.bot.Bot;
import com.uprzejmy.kod.bot.FarmerBot;
import com.uprzejmy.kod.bot.GoldMinerBot;
import com.uprzejmy.kod.kingdom.ResourceName;
import com.uprzejmy.kod.utils.KingdomBuilder;
import com.uprzejmy.kod.utils.KingdomPrinter;

public class BotGame
{
    @Test
    void simulateTenTurnsTest()
    {
        var game = new TestGame().get();
        var bots = new ArrayList<Bot>();
        var kingdomBuilder = new KingdomBuilder(game).withResource(ResourceName.turns, 10);
        kingdomBuilder = Utils.setupKingdomStartConfiguration(kingdomBuilder, game);

        var farmerKingdom = kingdomBuilder.withName("FarmerBot").build();
        Bot farmerBot = new FarmerBot(farmerKingdom);
        game.addKingdom(farmerKingdom);
        bots.add(farmerBot);

        var blacksmithKingdom = kingdomBuilder.withName("BlacksmithBot").build();
        Bot blacksmithBot = new BlacksmithBot(blacksmithKingdom);
        game.addKingdom(blacksmithKingdom);
        bots.add(blacksmithBot);

        var goldMinerKingdom1 = kingdomBuilder.withName("GoldMinerBot1").build();
        Bot goldMinerBot1 = new GoldMinerBot(goldMinerKingdom1);
        game.addKingdom(goldMinerKingdom1);
        bots.add(goldMinerBot1);

        var goldMinerKingdom2 = kingdomBuilder.withName("GoldMinerBot2").build();
        Bot goldMinerBot2 = new GoldMinerBot(goldMinerKingdom2);
        game.addKingdom(goldMinerKingdom2);
        bots.add(goldMinerBot2);

        KingdomPrinter.printResourcesHeader();
        KingdomPrinter.printLineSeparator();
        for (var bot : bots)
        {
            KingdomPrinter.kingdomInfoPrinter(bot.getKingdom());
        }
        KingdomPrinter.printLineSeparator();

        for (var i = 0; i < 10; i++)
        {
            for (var bot : bots)
            {
                bot.doAllActions();
                bot.passTurn();
                KingdomPrinter.kingdomInfoPrinter(bot.getKingdom());
            }
            KingdomPrinter.printLineSeparator();
        }
    }
}