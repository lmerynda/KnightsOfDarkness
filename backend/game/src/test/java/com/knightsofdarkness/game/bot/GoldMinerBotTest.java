package com.knightsofdarkness.game.bot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.game.Game;
import com.knightsofdarkness.game.TestGame;
import com.knightsofdarkness.game.kingdom.BuildingName;
import com.knightsofdarkness.game.kingdom.KingdomUnits;
import com.knightsofdarkness.game.kingdom.ResourceName;
import com.knightsofdarkness.game.kingdom.UnitName;
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
    @Disabled
    // TODO bring back option to run all training until it's not possible to train
    // anymore
    void afterTrainingNoNewUnitCanBeTrainedTest()
    {
        var kingdom = kingdomBuilder.build();
        var toTrain = new KingdomUnits();
        toTrain.addCount(UnitName.goldMiner, 1);

        var bot = new GoldMinerBot(kingdom, game.getMarket());
        bot.doActionCycle();

        var trainedUnits = kingdom.train(toTrain);

        assertEquals(0, trainedUnits.countAll());
    }
}