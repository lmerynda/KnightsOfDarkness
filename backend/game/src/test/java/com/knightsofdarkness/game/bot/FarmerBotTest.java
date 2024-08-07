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

public class FarmerBotTest {
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
    @Disabled
    void simulateTenTurnsTest()
    {
        var kingdom = kingdomBuilder.withResource(ResourceName.turns, 10).build();
        var farmersBefore = kingdom.getUnits().getCount(UnitName.farmer);
        var unusedLandBefore = kingdom.getUnusedLand();
        var farmsBefore = kingdom.getBuildings().getCount(BuildingName.farm);
        var housesBefore = kingdom.getBuildings().getCount(BuildingName.house);

        IBot bot = new FarmerBot(kingdom, game.getMarket());
        for (var i = 0; i < 10; i++)
        {
            bot.doActionCycle();
            bot.passTurn();
        }

        var farmersAfter = kingdom.getUnits().getCount(UnitName.farmer);
        var farmsAfter = kingdom.getBuildings().getCount(BuildingName.farm);
        var housesAfter = kingdom.getBuildings().getCount(BuildingName.house);
        assertTrue(farmersAfter > farmersBefore, "There were supposed to be new trained units, before " + farmersBefore + " after " + farmersAfter);
        assertTrue(kingdom.getUnusedLand() < unusedLandBefore, "The land was supposed to be used, available before " + unusedLandBefore + " after " + kingdom.getUnusedLand());
        assertTrue(farmsAfter > farmsBefore, "There were supposed to be new farms built, before " + farmsBefore + " after " + farmsAfter);
        assertTrue(housesAfter > housesBefore, "There were supposed to be new houses built, " + housesBefore + " after " + housesAfter);
    }

    @Test
    @Disabled
    // TODO make sure train is available
    void afterTrainingNoNewUnitCanBeTrainedTest()
    {
        var kingdom = kingdomBuilder.build();
        var toTrain = new KingdomUnits();
        toTrain.addCount(UnitName.farmer, 1);

        // var bot = new FarmerBot(kingdom);
        // bot.train();

        var trainedUnits = kingdom.train(toTrain);

        assertEquals(0, trainedUnits.countAll());
    }
}
