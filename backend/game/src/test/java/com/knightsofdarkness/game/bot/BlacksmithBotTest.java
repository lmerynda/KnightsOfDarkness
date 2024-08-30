package com.knightsofdarkness.game.bot;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.game.Game;
import com.knightsofdarkness.game.TestGame;
import com.knightsofdarkness.game.kingdom.BuildingName;
import com.knightsofdarkness.game.kingdom.ResourceName;
import com.knightsofdarkness.game.kingdom.UnitName;
import com.knightsofdarkness.game.utils.KingdomBuilder;
import com.knightsofdarkness.game.utils.KingdomPrinter;

class BlacksmithBotTest {
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
        var blacksmithsBefore = kingdom.getUnits().getCount(UnitName.blacksmith);
        var unusedLandBefore = kingdom.getUnusedLand();
        var workshopsBefore = kingdom.getBuildings().getCount(BuildingName.workshop);

        var bot = new BlacksmithBot(kingdom, game.getMarket());

        KingdomPrinter.printResourcesHeader();
        KingdomPrinter.printLineSeparator();
        for (var i = 0; i < 10; i++)
        {
            bot.doActionCycle();
            bot.passTurn();
            KingdomPrinter.kingdomInfoPrinter(kingdom);
        }

        var blacksmithsAfter = kingdom.getUnits().getCount(UnitName.blacksmith);
        var workshopsAfter = kingdom.getBuildings().getCount(BuildingName.workshop);
        assertTrue(blacksmithsAfter > blacksmithsBefore, "There were supposed to be new trained units, before " + blacksmithsBefore + " after " + blacksmithsAfter);
        assertTrue(kingdom.getUnusedLand() < unusedLandBefore, "The land was supposed to be used, available before " + unusedLandBefore + " after " + kingdom.getUnusedLand());
        assertTrue(workshopsAfter > workshopsBefore, "There were supposed to be new woirkshops built, before " + workshopsBefore + " after " + workshopsAfter);
    }
}