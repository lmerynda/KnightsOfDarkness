package com.knightsofdarkness.web.kingdom.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.common.kingdom.ResourceName;
import com.knightsofdarkness.common.kingdom.SpecialBuildingType;
import com.knightsofdarkness.web.Game;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.legacy.TestGame;
import com.knightsofdarkness.web.utils.Id;
import com.knightsofdarkness.web.utils.KingdomBuilder;

class KingdomSpecialBuildingActionTest {
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
    void whenKingdomNoSpecialBuildings_startingNew_shoulAddOneNewSpecialBuilding()
    {
        int initialSpecialBuildingsCount = kingdom.getSpecialBuildings().size();

        var action = new KingdomSpecialBuildingAction(kingdom, gameConfig);
        var specialBuilding = action.startSpecialBuilding(SpecialBuildingType.goldShaft);

        assertEquals(initialSpecialBuildingsCount + 1, kingdom.getSpecialBuildings().size());
        assertTrue(specialBuilding.isPresent());
        assertEquals(SpecialBuildingType.goldShaft, specialBuilding.get().buildingType);
    }

    @Test
    void whenKingdomHasMaxSpecialBuildings_startingNew_shouldNotAddNewSpecialBuilding()
    {
        int maxSpecialBuildings = gameConfig.common().specialBuildingMaxCount();
        var action = new KingdomSpecialBuildingAction(kingdom, gameConfig);
        for (int i = 0; i < maxSpecialBuildings; i++)
        {
            action.startSpecialBuilding(SpecialBuildingType.goldShaft);
        }
        int specialBuildingsCount = kingdom.getSpecialBuildings().size();

        action.startSpecialBuilding(SpecialBuildingType.goldShaft);

        assertEquals(specialBuildingsCount, kingdom.getSpecialBuildings().size());
    }

    @Test
    void whenKingdomHasNoSpecialBuildings_getLowestLevel_shouldReturnEmpty()
    {
        int specialBuildingsCount = kingdom.getSpecialBuildings().size();
        assertEquals(0, specialBuildingsCount);

        var action = new KingdomSpecialBuildingAction(kingdom, gameConfig);
        assertTrue(action.getLowestLevelSpecialBuilding().isEmpty());
    }

    @Test
    void whenKingdomHasOneSpecialBuildings_getLowestLevel_shouldReturnIt()
    {
        int specialBuildingsCount = kingdom.getSpecialBuildings().size();
        assertEquals(0, specialBuildingsCount);

        var action = new KingdomSpecialBuildingAction(kingdom, gameConfig);
        var newSpecialBuilding = action.startSpecialBuilding(SpecialBuildingType.goldShaft);

        assertTrue(newSpecialBuilding.isPresent());

        var lowestLevelSpecialBuilding = action.getLowestLevelSpecialBuilding();
        assertEquals(newSpecialBuilding.get().getId(), lowestLevelSpecialBuilding.get().getId());
    }

    @Test
    void whenKingdomHasOneSpecialBuildings_demolishingIt_shouldReduceNumberOfSpecialBuildingsByOne()
    {
        var action = new KingdomSpecialBuildingAction(kingdom, gameConfig);
        var newSpecialBuilding = action.startSpecialBuilding(SpecialBuildingType.goldShaft);
        int initialSpecialBuildingsCount = kingdom.getSpecialBuildings().size();
        assertEquals(1, initialSpecialBuildingsCount);

        action.demolishSpecialBuilding(newSpecialBuilding.get().getId());

        int currentSpecialBuildingsCount = kingdom.getSpecialBuildings().size();
        assertEquals(initialSpecialBuildingsCount - 1, currentSpecialBuildingsCount);
    }

    @Test
    void demolishingSpecialBuilding_withInvalidId_shouldNotChangeSpecialBuildingsCount()
    {
        var action = new KingdomSpecialBuildingAction(kingdom, gameConfig);
        action.startSpecialBuilding(SpecialBuildingType.goldShaft);
        int initialSpecialBuildingsCount = kingdom.getSpecialBuildings().size();
        assertEquals(1, initialSpecialBuildingsCount);

        action.demolishSpecialBuilding(Id.generate());

        int currentSpecialBuildingsCount = kingdom.getSpecialBuildings().size();
        assertEquals(initialSpecialBuildingsCount, currentSpecialBuildingsCount);
    }

    @Test
    void whenSpecialBuildingIsMaxLevel_buildSpecialBuilding_shouldNotSpendPoints()
    {
        var action = new KingdomSpecialBuildingAction(kingdom, gameConfig);
        var specialBuilding = action.startSpecialBuilding(SpecialBuildingType.goldShaft).get();
        specialBuilding.level = 5;
        specialBuilding.isMaxLevel = true;

        int pointsSpent = action.buildSpecialBuilding(specialBuilding, 100);

        assertEquals(0, pointsSpent);
    }

    @Test
    void whenBuildingPointsSufficient_buildSpecialBuilding_shouldCompleteBuilding()
    {
        var action = new KingdomSpecialBuildingAction(kingdom, gameConfig);
        var specialBuilding = action.startSpecialBuilding(SpecialBuildingType.goldShaft).get();
        specialBuilding.buildingPointsRequired = 100;
        specialBuilding.buildingPointsPut = 50;
        kingdom.getResources().addCount(ResourceName.buildingPoints, 100);

        int pointsSpent = action.buildSpecialBuilding(specialBuilding, 100);

        assertEquals(50, pointsSpent);
        assertEquals(0, specialBuilding.buildingPointsPut);
        assertEquals(1, specialBuilding.level);
    }

    @Test
    void whenBuildingPointsNotSufficient_buildSpecialBuilding_shouldPartiallyCompleteBuilding()
    {
        var action = new KingdomSpecialBuildingAction(kingdom, gameConfig);
        var specialBuilding = action.startSpecialBuilding(SpecialBuildingType.goldShaft).get();
        specialBuilding.buildingPointsRequired = 100;
        specialBuilding.buildingPointsPut = 50;
        kingdom.getResources().addCount(ResourceName.buildingPoints, 30);

        int pointsSpent = action.buildSpecialBuilding(specialBuilding, 30);

        assertEquals(30, pointsSpent);
        assertEquals(80, specialBuilding.buildingPointsPut);
        assertEquals(0, specialBuilding.level);
    }

    @Test
    void whenMaxLevelOfSpecialBuildingIsReached_completingBuilding_shouldSetBuildingPointsRequiredToZero()
    {
        var action = new KingdomSpecialBuildingAction(kingdom, gameConfig);
        var specialBuilding = action.startSpecialBuilding(SpecialBuildingType.goldShaft).get();
        specialBuilding.buildingPointsRequired = 100;
        specialBuilding.buildingPointsPut = 100;
        specialBuilding.level = 4;
        kingdom.getResources().addCount(ResourceName.buildingPoints, 100);

        action.buildSpecialBuilding(specialBuilding, 100);

        assertEquals(0, specialBuilding.buildingPointsRequired);
        assertTrue(specialBuilding.isMaxLevel);
    }
}
