package com.knightsofdarkness.web.kingdom.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.common.kingdom.BuildingName;
import com.knightsofdarkness.common.kingdom.ResourceName;
import com.knightsofdarkness.common.kingdom.UnitName;
import com.knightsofdarkness.common.kingdom.UnitsMapDto;
import com.knightsofdarkness.web.Game;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.legacy.TestGame;
import com.knightsofdarkness.web.utils.KingdomBuilder;

class KingdomTrainTest {
    private Game game;
    private GameConfig gameConfig;
    private KingdomBuilder kingdomBuilder;
    private KingdomEntity kingdom;

    @BeforeEach
    void setUp()
    {
        game = new TestGame().get();
        gameConfig = game.getConfig();
        kingdomBuilder = new KingdomBuilder(game);
        kingdom = kingdomBuilder.withRichConfiguration().build();
        game.addKingdom(kingdom);
    }

    @Test
    void trainSanityTest()
    {
        var toTrain = new UnitsMapDto();

        var action = new KingdomTrainAction(kingdom, gameConfig);
        var trainedUnits = action.train(toTrain).units();

        assertEquals(0, trainedUnits.countAll());
    }

    @Test
    void trainBasicTest()
    {
        kingdom.getBuildings().addCount(BuildingName.goldMine, 2);
        var toTrain = new UnitsMapDto();
        toTrain.setCount(UnitName.goldMiner, 10);

        var action = new KingdomTrainAction(kingdom, gameConfig);
        var trainedUnits = action.train(toTrain).units();

        assertEquals(10, trainedUnits.getCount(UnitName.goldMiner));
    }

    @Test
    void trainInsufficientGold()
    {
        kingdom.getResources().setCount(ResourceName.gold, 0);
        var toTrain = new UnitsMapDto();
        toTrain.setCount(UnitName.goldMiner, 10);

        var action = new KingdomTrainAction(kingdom, gameConfig);
        var trainedUnits = action.train(toTrain).units();

        assertEquals(0, trainedUnits.getCount(UnitName.goldMiner));
    }

    @Test
    void trainInsufficientTools()
    {
        kingdom.getResources().setCount(ResourceName.tools, 0);
        var toTrain = new UnitsMapDto();
        toTrain.setCount(UnitName.goldMiner, 10);

        var action = new KingdomTrainAction(kingdom, gameConfig);
        var trainedUnits = action.train(toTrain).units();

        assertEquals(0, trainedUnits.getCount(UnitName.goldMiner));
    }

    @Test
    void trainInsufficientUnemployed()
    {
        kingdom.getResources().setCount(ResourceName.unemployed, 0);
        var toTrain = new UnitsMapDto();
        toTrain.setCount(UnitName.goldMiner, 10);

        var action = new KingdomTrainAction(kingdom, gameConfig);
        var trainedUnits = action.train(toTrain).units();

        assertEquals(0, trainedUnits.getCount(UnitName.goldMiner));
    }

    @Test
    void trainPartiallySufficientGoldTest()
    {
        kingdom.getBuildings().addCount(BuildingName.goldMine, 2);

        kingdom.getResources().setCount(ResourceName.gold, 10000);
        var toTrain = new UnitsMapDto();
        toTrain.setCount(UnitName.goldMiner, 10);

        var action = new KingdomTrainAction(kingdom, gameConfig);
        var trainedUnits = action.train(toTrain).units();

        assertTrue(trainedUnits.countAll() > 0, "At least one unit should have been trained");
        assertTrue(trainedUnits.countAll() < 10, "Number of trained units was supposed to be smaller than desired");
    }

    @Test
    void trainAllUnitsTest()
    {
        for (var buildingName : BuildingName.values())
        {
            var oldCount = kingdom.getBuildings().getCount(buildingName);
            kingdom.getBuildings().setCount(buildingName, oldCount + 1);
        }
        var toTrain = new UnitsMapDto();
        for (var unitName : UnitName.values())
        {
            toTrain.setCount(unitName, 1);
        }

        var action = new KingdomTrainAction(kingdom, gameConfig);
        var trainedUnits = action.train(toTrain).units();

        for (var unitName : UnitName.values())
        {
            var trainedCount = trainedUnits.getCount(unitName);
            assertEquals(1, trainedCount, String.format("Expected %d, actual %d when training %s", 1, trainedCount, unitName));
        }
    }

    @Test
    void trainingShouldNotResultInLessUnits()
    {
        kingdom.getBuildings().setCount(BuildingName.goldMine, 0);
        var toTrain = new UnitsMapDto();
        toTrain.setCount(UnitName.goldMiner, 1);

        var action = new KingdomTrainAction(kingdom, gameConfig);
        var trainedUnits = action.train(toTrain).units();

        assertEquals(0, trainedUnits.getCount(UnitName.goldMiner));
    }

    @Test
    void trainInsufficientBuildingCapacityTest()
    {
        kingdom.getBuildings().setCount(BuildingName.goldMine, 0);
        kingdom.getUnits().setCount(UnitName.goldMiner, 0);
        var toTrain = new UnitsMapDto();
        toTrain.setCount(UnitName.goldMiner, 1);

        var action = new KingdomTrainAction(kingdom, gameConfig);
        var trainedUnits = action.train(toTrain).units();

        assertEquals(0, trainedUnits.getCount(UnitName.goldMiner));
    }
}
