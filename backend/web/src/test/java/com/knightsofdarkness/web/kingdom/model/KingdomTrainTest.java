package com.knightsofdarkness.web.kingdom.model;

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
import com.knightsofdarkness.web.legacy.TestGame;
import com.knightsofdarkness.web.utils.KingdomBuilder;

class KingdomTrainTest {
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
    void trainSanityTest()
    {
        var kingdom = kingdomBuilder.build();
        var toTrain = new UnitsMapDto();

        var trainedUnits = kingdom.train(toTrain).units();

        assertEquals(0, trainedUnits.countAll());
    }

    @Test
    void trainBasicTest()
    {
        var kingdom = kingdomBuilder.build();
        var toTrain = new UnitsMapDto();
        toTrain.setCount(UnitName.goldMiner, 10);

        var trainedUnits = kingdom.train(toTrain).units();

        assertEquals(10, trainedUnits.getCount(UnitName.goldMiner));
    }

    @Test
    void trainInsufficientGold()
    {
        var kingdom = kingdomBuilder.withResource(ResourceName.gold, 0).build();
        var toTrain = new UnitsMapDto();
        toTrain.setCount(UnitName.goldMiner, 10);

        var trainedUnits = kingdom.train(toTrain).units();

        assertEquals(0, trainedUnits.getCount(UnitName.goldMiner));
    }

    @Test
    void trainInsufficientTools()
    {
        var kingdom = kingdomBuilder.withResource(ResourceName.tools, 0).build();
        var toTrain = new UnitsMapDto();
        toTrain.setCount(UnitName.goldMiner, 10);

        var trainedUnits = kingdom.train(toTrain).units();

        assertEquals(0, trainedUnits.getCount(UnitName.goldMiner));
    }

    @Test
    void trainInsufficientUnemployed()
    {
        var kingdom = kingdomBuilder.withResource(ResourceName.unemployed, 0).build();
        var toTrain = new UnitsMapDto();
        toTrain.setCount(UnitName.goldMiner, 10);

        var trainedUnits = kingdom.train(toTrain).units();

        assertEquals(0, trainedUnits.getCount(UnitName.goldMiner));
    }

    @Test
    void trainPartiallySufficientGoldTest()
    {
        var kingdom = kingdomBuilder.withResource(ResourceName.gold, 10000).build();
        var toTrain = new UnitsMapDto();
        toTrain.setCount(UnitName.goldMiner, 10);

        var trainedUnits = kingdom.train(toTrain).units();

        assertTrue(trainedUnits.countAll() > 0, "At least one unit should have been trained");
        assertTrue(trainedUnits.countAll() < 10, "Number of trained units was supposed to be smaller than desired");
    }

    @Test
    void trainAllUnitsTest()
    {
        var kingdom = kingdomBuilder.withRichConfiguration().build();
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

        var trainedUnits = kingdom.train(toTrain).units();

        for (var unitName : UnitName.values())
        {
            var trainedCount = trainedUnits.getCount(unitName);
            assertEquals(1, trainedCount, String.format("Expected %d, actual %d when training %s", 1, trainedCount, unitName));
        }
    }

    @Test
    void trainingShouldNotResultInLessUnits()
    {
        var kingdom = kingdomBuilder.withBuilding(BuildingName.goldMine, 0).build();
        var toTrain = new UnitsMapDto();
        toTrain.setCount(UnitName.goldMiner, 1);

        var trainedUnits = kingdom.train(toTrain).units();

        assertEquals(0, trainedUnits.getCount(UnitName.goldMiner));
    }

    @Test
    void trainInsufficientBuildingCapacityTest()
    {
        var kingdom = kingdomBuilder.withBuilding(BuildingName.goldMine, 0).withUnit(UnitName.goldMiner, 0).build();
        var toTrain = new UnitsMapDto();
        toTrain.setCount(UnitName.goldMiner, 1);

        var trainedUnits = kingdom.train(toTrain).units();

        assertEquals(0, trainedUnits.getCount(UnitName.goldMiner));
    }
}
