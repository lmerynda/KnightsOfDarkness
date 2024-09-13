package com.knightsofdarkness.game.bot;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.common.kingdom.BuildingName;
import com.knightsofdarkness.common.kingdom.ResourceName;
import com.knightsofdarkness.common.kingdom.UnitName;
import com.knightsofdarkness.game.Game;
import com.knightsofdarkness.game.TestGame;
import com.knightsofdarkness.game.utils.KingdomBuilder;
import com.knightsofdarkness.game.utils.KingdomPrinter;

class IronMinerBotTest {
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
        var kingdom = kingdomBuilder.withResource(ResourceName.turns, 10).build();
        var ironMinersBefore = kingdom.getUnits().getCount(UnitName.ironMiner);
        var unusedLandBefore = kingdom.getUnusedLand();
        var ironMineBefore = kingdom.getBuildings().getCount(BuildingName.ironMine);

        var bot = new IronMinerBot(kingdom, game.getMarket());

        KingdomPrinter.printResourcesHeader();
        KingdomPrinter.printLineSeparator();
        for (var i = 0; i < 10; i++)
        {
            bot.doActionCycle();
            bot.passTurn();
            KingdomPrinter.kingdomInfoPrinter(kingdom);
        }

        var ironMinersAfter = kingdom.getUnits().getCount(UnitName.ironMiner);
        var ironMinesAfter = kingdom.getBuildings().getCount(BuildingName.ironMine);
        assertTrue(ironMinersAfter > ironMinersBefore, "There were supposed to be new trained units, before " + ironMinersBefore + " after " + ironMinersAfter);
        assertTrue(kingdom.getUnusedLand() < unusedLandBefore, "The land was supposed to be used, available before " + unusedLandBefore + " after " + kingdom.getUnusedLand());
        assertTrue(ironMinesAfter > ironMineBefore, "There were supposed to be new ironMines built, before " + ironMineBefore + " after " + ironMinesAfter);
    }
}