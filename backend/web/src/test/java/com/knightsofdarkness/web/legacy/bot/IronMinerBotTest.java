package com.knightsofdarkness.web.bots.legacy;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.common.kingdom.BuildingName;
import com.knightsofdarkness.common.kingdom.ResourceName;
import com.knightsofdarkness.common.kingdom.UnitName;
import com.knightsofdarkness.web.bots.legacy.IronMinerBot;
import com.knightsofdarkness.web.legacy.Game;
import com.knightsofdarkness.web.legacy.TestGame;
import com.knightsofdarkness.web.legacy.utils.KingdomBuilder;
import com.knightsofdarkness.web.legacy.utils.KingdomPrinter;

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
        var ironMinersBefore = kingdom.getUnits().getTotalCount(UnitName.ironMiner);
        var unusedLandBefore = kingdom.getUnusedLand();
        var ironMineBefore = kingdom.getBuildings().getCount(BuildingName.ironMine);

        var bot = new IronMinerBot(kingdom, game.getMarket(), game.getKingdomInteractor());

        KingdomPrinter.printResourcesHeader();
        KingdomPrinter.printLineSeparator();
        for (var i = 0; i < 10; i++)
        {
            bot.doActionCycle();
            bot.passTurn();
            KingdomPrinter.kingdomInfoPrinter(kingdom);
        }

        var ironMinersAfter = kingdom.getUnits().getTotalCount(UnitName.ironMiner);
        var ironMinesAfter = kingdom.getBuildings().getCount(BuildingName.ironMine);
        assertTrue(ironMinersAfter > ironMinersBefore, "There were supposed to be new trained units, before " + ironMinersBefore + " after " + ironMinersAfter);
        assertTrue(kingdom.getUnusedLand() < unusedLandBefore, "The land was supposed to be used, available before " + unusedLandBefore + " after " + kingdom.getUnusedLand());
        assertTrue(ironMinesAfter > ironMineBefore, "There were supposed to be new ironMines built, before " + ironMineBefore + " after " + ironMinesAfter);
    }
}
