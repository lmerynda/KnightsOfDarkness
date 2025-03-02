package com.knightsofdarkness.web.bots.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.common.kingdom.BuildingName;
import com.knightsofdarkness.common.kingdom.ResourceName;
import com.knightsofdarkness.common.kingdom.UnitName;
import com.knightsofdarkness.common.kingdom.UnitsMapDto;
import com.knightsofdarkness.web.bots.legacy.FarmerBot;
import com.knightsofdarkness.web.bots.legacy.IBot;
import com.knightsofdarkness.web.legacy.Game;
import com.knightsofdarkness.web.legacy.TestGame;
import com.knightsofdarkness.web.legacy.utils.KingdomBuilder;

class FarmerBotTest {
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
        var farmersBefore = kingdom.getUnits().getTotalCount(UnitName.farmer);
        var unusedLandBefore = kingdom.getUnusedLand();
        var farmsBefore = kingdom.getBuildings().getCount(BuildingName.farm);
        var housesBefore = kingdom.getBuildings().getCount(BuildingName.house);

        IBot bot = new FarmerBot(kingdom, game.getMarket(), game.getKingdomInteractor());
        for (var i = 0; i < 10; i++)
        {
            bot.doActionCycle();
            bot.passTurn();
        }

        var farmersAfter = kingdom.getUnits().getTotalCount(UnitName.farmer);
        var farmsAfter = kingdom.getBuildings().getCount(BuildingName.farm);
        var housesAfter = kingdom.getBuildings().getCount(BuildingName.house);
        assertTrue(farmersAfter > farmersBefore, "There were supposed to be new trained units, before " + farmersBefore + " after " + farmersAfter);
        assertTrue(kingdom.getUnusedLand() < unusedLandBefore, "The land was supposed to be used, available before " + unusedLandBefore + " after " + kingdom.getUnusedLand());
        assertTrue(farmsAfter > farmsBefore, "There were supposed to be new farms built, before " + farmsBefore + " after " + farmsAfter);
        assertTrue(housesAfter > housesBefore, "There were supposed to be new houses built, " + housesBefore + " after " + housesAfter);
    }

    @Test
    void afterTrainingNoNewUnitCanBeTrainedTest()
    {
        var kingdom = kingdomBuilder.build();
        game.addKingdom(kingdom);
        var toTrain = new UnitsMapDto();
        toTrain.setCount(UnitName.farmer, 1);

        var bot = new FarmerBot(kingdom, game.getMarket(), game.getKingdomInteractor());
        bot.doAllActions();

        var trainedUnits = kingdom.train(toTrain);

        assertEquals(0, trainedUnits.units().countAll());
    }
}
