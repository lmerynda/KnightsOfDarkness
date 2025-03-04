package com.knightsofdarkness.web.bots;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.common.kingdom.BuildingName;
import com.knightsofdarkness.common.kingdom.ResourceName;
import com.knightsofdarkness.common.kingdom.UnitName;
import com.knightsofdarkness.common.kingdom.UnitsMapDto;
import com.knightsofdarkness.web.Game;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.kingdom.model.KingdomTrainAction;
import com.knightsofdarkness.web.legacy.TestGame;
import com.knightsofdarkness.web.utils.KingdomBuilder;
import com.knightsofdarkness.web.utils.KingdomPrinter;

class GoldMinerBotTest {
    private static Game game;
    private static GameConfig gameConfig;
    private KingdomBuilder kingdomBuilder;

    @BeforeAll
    static void beforeAll()
    {
        game = new TestGame().get();
        gameConfig = game.getConfig();
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
        var goldMinersBefore = kingdom.getUnits().getTotalCount(UnitName.goldMiner);
        var unusedLandBefore = kingdom.getUnusedLand();
        var goldMinesBefore = kingdom.getBuildings().getCount(BuildingName.goldMine);

        var bot = new GoldMinerBot(kingdom, game.getMarket(), game.getKingdomInteractor(), gameConfig);

        KingdomPrinter.printResourcesHeader();
        KingdomPrinter.printLineSeparator();
        for (var i = 0; i < 36; i++)
        {
            bot.doActionCycle();
            bot.passTurn();
            KingdomPrinter.kingdomInfoPrinter(kingdom, gameConfig);
        }

        var goldMinersAfter = kingdom.getUnits().getTotalCount(UnitName.goldMiner);
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
        var toTrain = new UnitsMapDto();
        toTrain.setCount(UnitName.goldMiner, 1);

        var bot = new GoldMinerBot(kingdom, game.getMarket(), game.getKingdomInteractor(), gameConfig);
        bot.doAllActions();

        var action = new KingdomTrainAction(kingdom, gameConfig);
        var trainedUnits = action.train(toTrain);

        assertEquals(0, trainedUnits.units().countAll());
    }
}
