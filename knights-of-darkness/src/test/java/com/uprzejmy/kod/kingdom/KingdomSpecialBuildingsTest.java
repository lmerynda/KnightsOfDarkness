package com.uprzejmy.kod.kingdom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.uprzejmy.kod.TestGame;
import com.uprzejmy.kod.game.Game;
import com.uprzejmy.kod.utils.KingdomBuilder;

public class KingdomSpecialBuildingsTest
{
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
    void afterSpecialBuildingDeletion_buildingAtDeletedPlaceShouldBeEmpty()
    {
        int buildingPlace = 1;
        var specialBuildings = new KingdomSpecialBuildings(kingdomBuilder.build());
        specialBuildings.deleteBuilding(buildingPlace);
        assertEquals(SpecialBuildingName.emptyBuilding, specialBuildings.getAt(buildingPlace).get().buildingType);
    }

    @Test
    void afterSpecialBuildingStartAndDeletion_buildingAtDeletedPlaceShouldBeEmpty()
    {
        int buildingPlace = 1;
        var specialBuildings = new KingdomSpecialBuildings(kingdomBuilder.build());
        specialBuildings.startNew(SpecialBuildingName.goldShaft, 1);
        specialBuildings.deleteBuilding(buildingPlace);
        assertEquals(SpecialBuildingName.emptyBuilding, specialBuildings.getAt(buildingPlace).get().buildingType);
    }

    @Test
    void startBuilding_whenCurrentBuildingAtPlaceIsEmpty_shouldStartNewOne()
    {
        int buildingPlace = 1;
        var specialBuildings = new KingdomSpecialBuildings(kingdomBuilder.build());
        specialBuildings.startNew(SpecialBuildingName.goldShaft, 1);
        assertEquals(SpecialBuildingName.goldShaft, specialBuildings.getAt(buildingPlace).get().buildingType);
    }

    @Test
    void startBuilding_whenCurrentBuildingAtPlaceIsNotEmpty_shouldDoNothing()
    {
        int buildingPlace = 1;
        var specialBuildings = new KingdomSpecialBuildings(kingdomBuilder.build());
        specialBuildings.startNew(SpecialBuildingName.goldShaft, 1);
        var currentBuilding = specialBuildings.getAt(buildingPlace);
        specialBuildings.startNew(SpecialBuildingName.ironShaft, 1);
        assertEquals(currentBuilding, specialBuildings.getAt(buildingPlace));
    }

    @Test
    void kingdomSpecialBuildings_initially_shouldHaveAllSpecialBuildingsEmpty()
    {
        var specialBuildings = new KingdomSpecialBuildings(kingdomBuilder.build());

        // TODO move magic value to config
        for (int buildingPlace = 1; buildingPlace <= 5; buildingPlace++)
        {
            assertEquals(SpecialBuildingName.emptyBuilding, specialBuildings.getAt(buildingPlace).get().buildingType);
        }
    }

    @Test
    void kingdomSpecialBuildings_whenInvalidPlace_shouldNotReturnAnything()
    {
        int nonExistingPlace = 9999;
        var specialBuildings = new KingdomSpecialBuildings(kingdomBuilder.build());

        assertTrue(specialBuildings.getAt(nonExistingPlace).isEmpty());
    }

    /*
     * checks zero-index case as buildings should be indexed starting at 1 position
     */
    @Test
    void kingdomSpecialBuildings_whenAskedForBuildingAtZeroPlace_shouldNotReturnAnything()
    {
        int nonExistingPlace = 0;
        var specialBuildings = new KingdomSpecialBuildings(kingdomBuilder.build());

        assertTrue(specialBuildings.getAt(nonExistingPlace).isEmpty());
    }
}
