package com.knightsofdarkness.game.bot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.common.kingdom.BuildingName;
import com.knightsofdarkness.common.kingdom.KingdomUnitsDto;
import com.knightsofdarkness.common.kingdom.ResourceName;
import com.knightsofdarkness.common.kingdom.UnitName;
import com.knightsofdarkness.game.Game;
import com.knightsofdarkness.game.TestGame;
import com.knightsofdarkness.game.utils.KingdomBuilder;
import com.knightsofdarkness.game.utils.KingdomPrinter;

class GoldMinerBotTest {
    private static Game game;
    private KingdomBuilder kingdomBuilder;

    @BeforeAll
    static void beforeAll()
    {
        game = new TestGame().get();
    }

    @BeforeEach
    void setUp()
    {
        this.kingdomBuilder = new KingdomBuilder(game);
    }

    @Test
    void simulateTenTurnsTest()
    {
        var kingdom = kingdomBuilder.withResource(ResourceName.turns, 36).build();
        var goldMinersBefore = kingdom.getUnits().getCount(UnitName.goldMiner);
        var unusedLandBefore = kingdom.getUnusedLand();
        var goldMinesBefore = kingdom.getBuildings().getCount(BuildingName.goldMine);

        var bot = new GoldMinerBot(kingdom, game.getMarket());

        KingdomPrinter.printResourcesHeader();
        KingdomPrinter.printLineSeparator();
        for (var i = 0; i < 36; i++)
        {
            bot.doActionCycle();
            bot.passTurn();
            KingdomPrinter.kingdomInfoPrinter(kingdom);
        }

        var goldMinersAfter = kingdom.getUnits().getCount(UnitName.goldMiner);
        var goldMinesAfter = kingdom.getBuildings().getCount(BuildingName.goldMine);
        assertTrue(goldMinersAfter > goldMinersBefore, "There were supposed to be new trained units, before " + goldMinersBefore + " after " + goldMinersAfter);
        assertTrue(kingdom.getUnusedLand() < unusedLandBefore, "The land was supposed to be used, available before " + unusedLandBefore + " after " + kingdom.getUnusedLand());
        assertTrue(goldMinesAfter > goldMinesBefore, "There were supposed to be new gold mines built, before " + goldMinesBefore + " after " + goldMinesAfter);
    }

    @Test
    void afterTrainingNoNewUnitCanBeTrainedTest()
    {
        var kingdom = kingdomBuilder.build();
        game.addKingdom(kingdom);
        var toTrain = new KingdomUnitsDto();
        toTrain.setCount(UnitName.goldMiner, 1);

        var bot = new GoldMinerBot(kingdom, game.getMarket());
        bot.doAllActions();

        var trainedUnits = kingdom.train(toTrain);

        assertEquals(0, trainedUnits.units().countAll());
    }
}