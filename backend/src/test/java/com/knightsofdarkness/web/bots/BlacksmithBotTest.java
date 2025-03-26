package com.knightsofdarkness.web.bots;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.web.Game;
import com.knightsofdarkness.web.common.kingdom.BuildingName;
import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.kingdom.UnitName;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.legacy.TestGame;
import com.knightsofdarkness.web.utils.KingdomBuilder;
import com.knightsofdarkness.web.utils.KingdomPrinter;

class BlacksmithBotTest {
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
        var kingdom = kingdomBuilder.withResource(ResourceName.turns, 10).build();
        // pump it with iron, to maintain upkeep
        kingdom.getResources().addCount(ResourceName.iron, 10000);
        var blacksmithsBefore = kingdom.getUnits().getTotalCount(UnitName.blacksmith);
        var unusedLandBefore = kingdom.getUnusedLand();
        var workshopsBefore = kingdom.getBuildings().getCount(BuildingName.workshop);

        var bot = new BlacksmithBot(kingdom, game.getMarket(), game.getKingdomInteractor(), gameConfig);

        KingdomPrinter.printResourcesHeader();
        KingdomPrinter.printLineSeparator();
        for (var i = 0; i < 10; i++)
        {
            bot.doActionCycle();
            bot.passTurn();
            KingdomPrinter.kingdomInfoPrinter(kingdom, gameConfig);
        }

        var blacksmithsAfter = kingdom.getUnits().getTotalCount(UnitName.blacksmith);
        var workshopsAfter = kingdom.getBuildings().getCount(BuildingName.workshop);
        // TODO use assertThat for simpler assert interface
        assertTrue(blacksmithsAfter > blacksmithsBefore, "There were supposed to be new trained units, before " + blacksmithsBefore + " after " + blacksmithsAfter);
        assertTrue(kingdom.getUnusedLand() < unusedLandBefore, "The land was supposed to be used, available before " + unusedLandBefore + " after " + kingdom.getUnusedLand());
        assertTrue(workshopsAfter > workshopsBefore, "There were supposed to be new workshops built, before " + workshopsBefore + " after " + workshopsAfter);
    }
}
