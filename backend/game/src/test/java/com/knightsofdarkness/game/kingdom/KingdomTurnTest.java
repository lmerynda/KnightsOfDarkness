package com.knightsofdarkness.game.kingdom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.game.Game;
import com.knightsofdarkness.game.TestGame;
import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.game.utils.KingdomBuilder;

class KingdomTurnTest {
    private static Game game;
    private static GameConfig config;

    private KingdomBuilder kingdomBuilder;

    @BeforeAll
    static void beforeAll()
    {
        game = new TestGame().get();
        config = game.getConfig();
    }

    @BeforeEach
    void setUp()
    {
        this.kingdomBuilder = new KingdomBuilder(game);
    }

    @Test
    void passTurnSanityTest()
    {
        var kingdom = kingdomBuilder.withUnit(UnitName.goldMiner, 0).build();
        Map<ResourceName, Integer> resourcesBeforeTurn = new EnumMap<>(kingdom.getResources().resources);

        kingdom.passTurn();

        assertEquals(resourcesBeforeTurn.get(ResourceName.gold), kingdom.getResources().getCount(ResourceName.gold));
    }

    @Test
    void passTurnGoldMinersProductionTest()
    {
        var goldMinersCount = 10;
        var kingdom = kingdomBuilder
                .withUnit(UnitName.goldMiner, goldMinersCount)
                .withLand(1000) // to avoid small kingdom bonus
                .withBuilding(BuildingName.goldMine, 1000)
                .build();
        Map<ResourceName, Integer> resourcesBeforeTurn = new EnumMap<>(kingdom.getResources().resources);

        kingdom.passTurn();

        var goldMinerProductionRate = config.production().getProductionRate(UnitName.goldMiner);
        var newProduction = goldMinersCount * goldMinerProductionRate;
        assertEquals(resourcesBeforeTurn.get(ResourceName.gold) + newProduction, kingdom.getResources().getCount(ResourceName.gold));
    }

    @Test
    void passTurn_withEnoughIron_thenBlacksmithsShouldProduceToolsAtFullRate()
    {
        var blacksmithCount = 10;
        var blacksmithProductionRate = config.production().getProductionRate(UnitName.blacksmith);
        var kingdom = kingdomBuilder
                .withUnit(UnitName.blacksmith, blacksmithCount)
                .withLand(1000) // to avoid small kingdom bonus
                .withBuilding(BuildingName.workshop, 1000)
                .build();
        kingdom.getResources().setCount(ResourceName.iron, kingdom.getIronUpkeep(1.0));
        Map<ResourceName, Integer> resourcesBeforeTurn = new EnumMap<>(kingdom.getResources().resources);

        kingdom.passTurn();

        var newProduction = blacksmithCount * blacksmithProductionRate;

        assertEquals(resourcesBeforeTurn.get(ResourceName.tools) + newProduction, kingdom.getResources().getCount(ResourceName.tools));
    }

    @Test
    void passTurn_withEnoughIronAndNoIronProduction_shouldConsumeExactAmountOfIron()
    {
        var blacksmithCount = 10;
        var kingdom = kingdomBuilder.withUnit(UnitName.blacksmith, blacksmithCount).withUnit(UnitName.ironMiner, 0).build();
        var ironUpkeep = kingdom.getIronUpkeep(1.0);
        kingdom.getResources().setCount(ResourceName.iron, ironUpkeep);
        Map<ResourceName, Integer> resourcesBeforeTurn = new EnumMap<>(kingdom.getResources().resources);

        kingdom.passTurn();

        assertEquals(resourcesBeforeTurn.get(ResourceName.iron) - ironUpkeep, kingdom.getResources().getCount(ResourceName.iron));
    }

    @Test
    void passTurn_withNotEnoughIronAndNoIronProduction_shouldConsumeAllTheRemainingIron()
    {
        var blacksmithCount = 10;
        var kingdom = kingdomBuilder
                .withUnit(UnitName.blacksmith, blacksmithCount)
                .withUnit(UnitName.ironMiner, 0)
                .withLand(1000) // to avoid small kingdom bonus
                .withBuilding(BuildingName.workshop, 1000)
                .build();
        kingdom.getResources().setCount(ResourceName.iron, kingdom.getIronUpkeep(1.0) - 1);

        kingdom.passTurn();

        assertEquals(0, kingdom.getResources().getCount(ResourceName.iron));
    }

    @Test
    void passTurn_withNotEnoughIronAndNoIronProduction_shouldProduceToolsAtProportionallySmallerRate()
    {
        var blacksmithCount = 10;
        var blacksmithProductionRate = config.production().getProductionRate(UnitName.blacksmith);
        var kingdom = kingdomBuilder
                .withUnit(UnitName.blacksmith, blacksmithCount)
                .withUnit(UnitName.ironMiner, 0)
                .withLand(1000) // to avoid small kingdom bonus
                .withBuilding(BuildingName.workshop, 1000)
                .build();
        kingdom.getResources().setCount(ResourceName.iron, kingdom.getIronUpkeep(1.0) / 2);
        Map<ResourceName, Integer> resourcesBeforeTurn = new EnumMap<>(kingdom.getResources().resources);

        kingdom.passTurn();

        var newProduction = blacksmithCount * blacksmithProductionRate / 2;

        assertEquals(resourcesBeforeTurn.get(ResourceName.tools) + newProduction, kingdom.getResources().getCount(ResourceName.tools));
    }

    @Test
    void passTurn_withZeroIronAndSufficientIronProductionForBlacksmiths_shouldProduceToolsAtFullRate()
    {
        var blacksmithCount = 10;
        var blacksmithProductionRate = config.production().getProductionRate(UnitName.blacksmith);
        var ironMinerProductionRate = config.production().getProductionRate(UnitName.ironMiner);
        var ironToSpendPerToolProduction = 1;
        var ironMinerCount = blacksmithCount * blacksmithProductionRate / ironMinerProductionRate * ironToSpendPerToolProduction;
        var kingdom = kingdomBuilder
                .withUnit(UnitName.blacksmith, blacksmithCount)
                .withUnit(UnitName.ironMiner, ironMinerCount)
                .withLand(1000) // to avoid small kingdom bonus
                .withBuilding(BuildingName.workshop, 1000)
                .build();
        Map<ResourceName, Integer> resourcesBeforeTurn = new EnumMap<>(kingdom.getResources().resources);

        kingdom.passTurn();

        var newProduction = blacksmithCount * blacksmithProductionRate;

        assertEquals(resourcesBeforeTurn.get(ResourceName.tools) + newProduction, kingdom.getResources().getCount(ResourceName.tools));
    }

    @Test
    void passTurn_withZeroIronAndSufficientIronProductionForBlacksmiths_shouldRemainWithZeroIron()
    {
        var blacksmithCount = 10;
        var blacksmithProductionRate = config.production().getProductionRate(UnitName.blacksmith);
        var ironMinerProductionRate = config.production().getProductionRate(UnitName.ironMiner);
        var ironToSpendPerToolProduction = 1;
        var ironMinerCount = blacksmithCount * blacksmithProductionRate / ironMinerProductionRate * ironToSpendPerToolProduction;
        var kingdom = kingdomBuilder
                .withUnit(UnitName.blacksmith, blacksmithCount)
                .withUnit(UnitName.ironMiner, ironMinerCount)
                .withResource(ResourceName.iron, 0)
                .withLand(1000) // to avoid small kingdom bonus
                .withBuilding(BuildingName.workshop, 1000)
                .build();

        kingdom.passTurn();

        assertEquals(0, kingdom.getResources().getCount(ResourceName.iron));
    }

    @Test
    void passTurnAllProductionTest()
    {
        for (var unitName : UnitName.getProductionUnits())
        {
            kingdomBuilder.withUnit(unitName, 10);
        }

        var kingdom = kingdomBuilder.build();
        // first turn pass to clear the initial resources count, building points is a bit of a special case
        kingdom.passTurn();
        Map<ResourceName, Integer> resourcesBeforeTurn = new EnumMap<>(kingdom.getResources().resources);
        // count food for upkeep
        resourcesBeforeTurn.put(ResourceName.food, resourcesBeforeTurn.get(ResourceName.food) - kingdom.getFoodUpkeep());
        // count iron for upkeep
        resourcesBeforeTurn.put(ResourceName.iron, resourcesBeforeTurn.get(ResourceName.iron) - kingdom.getIronUpkeep(1.0));
        // building points will just refresh on new turn, remove them by one to make sureafter new turn we have more
        resourcesBeforeTurn.put(ResourceName.buildingPoints, resourcesBeforeTurn.get(ResourceName.buildingPoints) - 1);
        kingdom.passTurn();

        for (var unitName : UnitName.getProductionUnits())
        {
            var resourceName = config.production().getResource(unitName);
            var countBeforeTurn = resourcesBeforeTurn.get(resourceName);
            var countAfterTurn = kingdom.getResources().getCount(resourceName);
            assertThat(countBeforeTurn)
                    .withFailMessage("%s after production %d should not be lower than before %s", resourceName, countAfterTurn, countBeforeTurn)
                    .isLessThan(countAfterTurn);
        }
    }

    @Test
    void passTurnNewPeopleTest()
    {
        var kingdom = kingdomBuilder.withResource(ResourceName.unemployed, 0).withBuilding(BuildingName.house, 99999).build();
        Map<ResourceName, Integer> resourcesBeforeTurn = new EnumMap<>(kingdom.getResources().resources);

        kingdom.passTurn();

        var beforeTurn = resourcesBeforeTurn.get(ResourceName.unemployed);
        var afterTurn = kingdom.getResources().getCount(ResourceName.unemployed);
        assertTrue(beforeTurn < afterTurn, "Expected number of unemployed before turn " + beforeTurn + " to be lower than after turn " + afterTurn);
    }

    @Test
    void passTurn_WithFullHousingCapacity_NewPeopleShouldNotArriveTest()
    {
        var kingdom = kingdomBuilder.withResource(ResourceName.unemployed, 9999).build();
        Map<ResourceName, Integer> resourcesBeforeTurn = new EnumMap<>(kingdom.getResources().resources);

        kingdom.passTurn();

        var beforeTurn = resourcesBeforeTurn.get(ResourceName.unemployed);
        var afterTurn = kingdom.getResources().getCount(ResourceName.unemployed);
        assertTrue(beforeTurn >= afterTurn, "Expected number of unemployed before turn " + beforeTurn + " to NOT be lower than after turn " + afterTurn);
    }

    @Test
    void passMoreTurnsThanMaximumTest()
    {
        var kingdom = kingdomBuilder.build();
        var numberOfTurns = kingdom.getResources().getCount(ResourceName.turns);

        // first we pass every turn that kingdom has
        for (int i = 0; i < numberOfTurns; i++)
        {
            assertTrue(kingdom.passTurn().isPresent());
        }

        // then we try to pass turn which is not available
        assertTrue(kingdom.passTurn().isEmpty());
    }
}