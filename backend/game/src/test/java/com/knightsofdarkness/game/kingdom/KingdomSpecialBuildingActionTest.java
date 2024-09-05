package com.knightsofdarkness.game.kingdom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.game.Game;
import com.knightsofdarkness.game.Id;
import com.knightsofdarkness.game.TestGame;
import com.knightsofdarkness.game.utils.KingdomBuilder;

class KingdomSpecialBuildingActionTest {
    private static Game game;
    private KingdomBuilder kingdomBuilder;
    private Kingdom kingdom;

    @BeforeAll
    static void beforeAll()
    {
        game = new TestGame().get();
    }

    @BeforeEach
    void setUp()
    {
        kingdomBuilder = new KingdomBuilder(game);
        kingdom = kingdomBuilder.build();
    }

    @Test
    void whenKingdomNoSpecialBuildings_startingNew_shoulAddOneNewSpecialBuilding()
    {
        int initialSpecialBuildingsCount = kingdom.getSpecialBuildings().size();

        var specialBuilding = kingdom.startSpecialBuilding(SpecialBuildingType.goldShaft);

        assertEquals(initialSpecialBuildingsCount + 1, kingdom.getSpecialBuildings().size());
        assertTrue(specialBuilding.isPresent());
        assertEquals(SpecialBuildingType.goldShaft, specialBuilding.get().buildingType);
    }

    @Test
    void whenKingdomHasMaxSpecialBuildings_startingNew_shouldNotAddNewSpecialBuilding()
    {
        int maxSpecialBuildings = kingdom.getConfig().common().specialBuildingMaxCount();
        for (int i = 0; i < maxSpecialBuildings; i++)
        {
            kingdom.startSpecialBuilding(SpecialBuildingType.goldShaft);
        }
        int specialBuildingsCount = kingdom.getSpecialBuildings().size();

        kingdom.startSpecialBuilding(SpecialBuildingType.goldShaft);

        assertEquals(specialBuildingsCount, kingdom.getSpecialBuildings().size());
    }

    @Test
    void whenKingdomHasNoSpecialBuildings_getLowestLevel_shouldReturnEmpty()
    {
        int specialBuildingsCount = kingdom.getSpecialBuildings().size();
        assertEquals(0, specialBuildingsCount);

        assertTrue(kingdom.getLowestLevelSpecialBuilding().isEmpty());
    }

    @Test
    void whenKingdomHasOneSpecialBuildings_getLowestLevel_shouldReturnIt()
    {
        int specialBuildingsCount = kingdom.getSpecialBuildings().size();
        assertEquals(0, specialBuildingsCount);

        var newSpecialBuilding = kingdom.startSpecialBuilding(SpecialBuildingType.goldShaft);

        assertTrue(newSpecialBuilding.isPresent());

        var lowestLevelSpecialBuilding = kingdom.getLowestLevelSpecialBuilding();
        assertEquals(newSpecialBuilding.get().getId(), lowestLevelSpecialBuilding.get().getId());
    }

    @Test
    void whenKingdomHasOneSpecialBuildings_demolishingIt_shouldReduceNumberOfSpecialBuildingsByOne()
    {
        var newSpecialBuilding = kingdom.startSpecialBuilding(SpecialBuildingType.goldShaft);
        int initialSpecialBuildingsCount = kingdom.getSpecialBuildings().size();
        assertEquals(1, initialSpecialBuildingsCount);

        kingdom.demolishSpecialBuilding(newSpecialBuilding.get().getId());

        int currentSpecialBuildingsCount = kingdom.getSpecialBuildings().size();
        assertEquals(initialSpecialBuildingsCount - 1, currentSpecialBuildingsCount);
    }

    @Test
    void demolishingSpecialBuilding_withInvalidId_shouldNotChangeSpecialBuildingsCount()
    {
        kingdom.startSpecialBuilding(SpecialBuildingType.goldShaft);
        int initialSpecialBuildingsCount = kingdom.getSpecialBuildings().size();
        assertEquals(1, initialSpecialBuildingsCount);

        kingdom.demolishSpecialBuilding(Id.generate());

        int currentSpecialBuildingsCount = kingdom.getSpecialBuildings().size();
        assertEquals(initialSpecialBuildingsCount, currentSpecialBuildingsCount);
    }
}
