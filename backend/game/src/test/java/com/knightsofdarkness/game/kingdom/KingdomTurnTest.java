package com.knightsofdarkness.game.kingdom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.common.kingdom.BuildingName;
import com.knightsofdarkness.common.kingdom.KingdomBuildingsDto;
import com.knightsofdarkness.common.kingdom.ResourceName;
import com.knightsofdarkness.common.kingdom.UnitName;
import com.knightsofdarkness.common.kingdom.UnitsMapDto;
import com.knightsofdarkness.game.Game;
import com.knightsofdarkness.game.TestGame;
import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.game.utils.KingdomBuilder;

class KingdomTurnTest {
    private static Game game;
    private static GameConfig config;
    private KingdomBuilder kingdomBuilder;
    private static final int weaponsProductionPercentage = 0;

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

        kingdom.passTurn(game.getKingdomInteractor(), weaponsProductionPercentage);

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

        kingdom.passTurn(game.getKingdomInteractor(), weaponsProductionPercentage);

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

        kingdom.passTurn(game.getKingdomInteractor(), weaponsProductionPercentage);

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

        kingdom.passTurn(game.getKingdomInteractor(), weaponsProductionPercentage);

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

        kingdom.passTurn(game.getKingdomInteractor(), weaponsProductionPercentage);

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

        kingdom.passTurn(game.getKingdomInteractor(), weaponsProductionPercentage);

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

        kingdom.passTurn(game.getKingdomInteractor(), weaponsProductionPercentage);

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

        kingdom.passTurn(game.getKingdomInteractor(), weaponsProductionPercentage);

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
        kingdom.passTurn(game.getKingdomInteractor(), weaponsProductionPercentage);
        Map<ResourceName, Integer> resourcesBeforeTurn = new EnumMap<>(kingdom.getResources().resources);
        // count food for upkeep
        resourcesBeforeTurn.put(ResourceName.food, resourcesBeforeTurn.get(ResourceName.food) - kingdom.getFoodUpkeep());
        // count iron for upkeep
        resourcesBeforeTurn.put(ResourceName.iron, resourcesBeforeTurn.get(ResourceName.iron) - kingdom.getIronUpkeep(1.0));
        // building points will just refresh on new turn, remove them by one to make sureafter new turn we have more
        resourcesBeforeTurn.put(ResourceName.buildingPoints, resourcesBeforeTurn.get(ResourceName.buildingPoints) - 1);
        kingdom.passTurn(game.getKingdomInteractor(), weaponsProductionPercentage);

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

        kingdom.passTurn(game.getKingdomInteractor(), weaponsProductionPercentage);

        var beforeTurn = resourcesBeforeTurn.get(ResourceName.unemployed);
        var afterTurn = kingdom.getResources().getCount(ResourceName.unemployed);
        assertTrue(beforeTurn < afterTurn, "Expected number of unemployed before turn " + beforeTurn + " to be lower than after turn " + afterTurn);
    }

    @Test
    void passTurn_WithFullHousingCapacity_NewPeopleShouldNotArriveTest()
    {
        var kingdom = kingdomBuilder.withResource(ResourceName.unemployed, 9999).build();
        Map<ResourceName, Integer> resourcesBeforeTurn = new EnumMap<>(kingdom.getResources().resources);

        kingdom.passTurn(game.getKingdomInteractor(), weaponsProductionPercentage);

        var beforeTurn = resourcesBeforeTurn.get(ResourceName.unemployed);
        var afterTurn = kingdom.getResources().getCount(ResourceName.unemployed);
        assertTrue(beforeTurn >= afterTurn, "Expected number of unemployed before turn " + beforeTurn + " to NOT be lower than after turn " + afterTurn);
    }

    @Test
    void passTurn_withInsufficientHousing_shouldSendPeopleToExile()
    {
        var kingdom = kingdomBuilder.build();
        kingdom.passTurn(game.getKingdomInteractor(), weaponsProductionPercentage);

        var beforeTurn = kingdom.getResources().getCount(ResourceName.unemployed);
        kingdom.getBuildings().subtractCount(BuildingName.house, 1);
        kingdom.passTurn(game.getKingdomInteractor(), weaponsProductionPercentage);

        var afterTurn = kingdom.getResources().getCount(ResourceName.unemployed);
        assertTrue(beforeTurn > afterTurn, "Expected number of unemployed to reduce due to insufficient housing");
    }

    @Test
    void passMoreTurnsThanMaximumTest()
    {
        var kingdom = kingdomBuilder.build();
        var numberOfTurns = kingdom.getResources().getCount(ResourceName.turns);

        // first we pass every turn that kingdom has
        for (int i = 0; i < numberOfTurns; i++)
        {
            assertTrue(kingdom.passTurn(game.getKingdomInteractor(), weaponsProductionPercentage).success());
        }

        // then we try to pass turn which is not available
        assertFalse(kingdom.passTurn(game.getKingdomInteractor(), weaponsProductionPercentage).success());
    }

    @Test
    void whenTurnCountBelowMaximum_addingTurn_shouldResultInMoreTurnsAvailable()
    {
        var kingdom = kingdomBuilder.build();
        var numberOfTurns = kingdom.getResources().getCount(ResourceName.turns);

        kingdom.addTurn();

        assertEquals(numberOfTurns + 1, kingdom.getResources().getCount(ResourceName.turns));
    }

    @Test
    void whenTurnCountReachesMaximum_addingTurn_shouldNotChangeTurnCount()
    {
        var kingdom = kingdomBuilder.withResource(ResourceName.turns, config.common().maxTurns()).build();
        var numberOfTurns = kingdom.getResources().getCount(ResourceName.turns);

        kingdom.addTurn();

        assertEquals(numberOfTurns, kingdom.getResources().getCount(ResourceName.turns));
    }

    @Test
    void passingTurn_shouldSaveReport()
    {
        var kingdom = kingdomBuilder.build();
        var result = kingdom.passTurn(game.getKingdomInteractor(), weaponsProductionPercentage);
        assertTrue(result.success());

        var savedReport = kingdom.getLastTurnReport();

        assertEquals(result.turnReport().get(), savedReport);
    }

    @Test
    void passTurn_withInsufficientProfessionalBuildingCapacity_shouldFireExceedingNumber()
    {
        var kingdom = kingdomBuilder.build();
        kingdom.passTurn(game.getKingdomInteractor(), weaponsProductionPercentage);

        trainGoldMinersToReachMaxCapacity(kingdom);

        var unemployedBeforeTurn = kingdom.getResources().getCount(ResourceName.unemployed);
        var goldMinersBeforeTurnCount = kingdom.getUnits().getTotalCount(UnitName.goldMiner);
        var toDemolish = new KingdomBuildingsDto();
        toDemolish.setCount(BuildingName.goldMine, 1);
        kingdom.demolish(toDemolish);
        kingdom.passTurn(game.getKingdomInteractor(), weaponsProductionPercentage);

        var unemployedAfterTurn = kingdom.getResources().getCount(ResourceName.unemployed);
        var goldMinersAfterTurnCount = kingdom.getUnits().getTotalCount(UnitName.goldMiner);
        assertThat(unemployedAfterTurn).isGreaterThan(unemployedBeforeTurn);
        assertThat(goldMinersAfterTurnCount).isLessThan(goldMinersBeforeTurnCount);
        assertThat(unemployedAfterTurn - unemployedBeforeTurn).isEqualTo(goldMinersBeforeTurnCount - goldMinersAfterTurnCount);
    }

    @Test
    void passTurn_withInsufficientHousesAndEnoughBuildingCapacity_shouldExileOnlyUnemployed()
    {
        var kingdom = kingdomBuilder.build();
        kingdom.passTurn(game.getKingdomInteractor(), weaponsProductionPercentage);

        var unemployedBeforeTurn = kingdom.getResources().getCount(ResourceName.unemployed);
        var toDemolish = new KingdomBuildingsDto();
        toDemolish.setCount(BuildingName.house, 1);
        kingdom.demolish(toDemolish);

        kingdom.passTurn(game.getKingdomInteractor(), weaponsProductionPercentage);

        var unemployedAfterTurn = kingdom.getResources().getCount(ResourceName.unemployed);
        assertThat(unemployedAfterTurn).isLessThan(unemployedBeforeTurn);
    }

    private void trainGoldMinersToReachMaxCapacity(Kingdom kingdom)
    {
        var currentGoldMinersCount = kingdom.getUnits().getTotalCount(UnitName.goldMiner);
        var goldMinersCapacity = kingdom.getBuildingCapacity(BuildingName.goldMine);
        var unitsToTrain = new UnitsMapDto();
        unitsToTrain.setCount(UnitName.goldMiner, goldMinersCapacity - currentGoldMinersCount);
        kingdom.train(unitsToTrain);
        kingdom.passTurn(game.getKingdomInteractor(), weaponsProductionPercentage);
    }

    @Test
    void testPeopleLeavingDueToInsufficientHousing_noExileWhenHousingIsSufficient()
    {
        Kingdom kingdom = kingdomBuilder
                .withUnit(UnitName.goldMiner, 10)
                .withBuilding(BuildingName.house, 25)
                .build();

        var result = kingdom.passTurn(game.getKingdomInteractor(), weaponsProductionPercentage);

        assertEquals(0, result.turnReport().get().exiledPeople);
    }

    @Test
    void testPeopleLeavingDueToInsufficientHousing_exileWhenHousingIsInsufficient()
    {
        Kingdom kingdom = kingdomBuilder
                .withBuilding(BuildingName.house, 1)
                .build();

        var result = kingdom.passTurn(game.getKingdomInteractor(), weaponsProductionPercentage);

        assertEquals(0, kingdom.getResources().getCount(ResourceName.unemployed));
        assertThat(result.turnReport().get().exiledPeople).isPositive();
    }

    @Test
    void testPeopleLeavingDueToInsufficientHousing_partialExileWhenHousingIsPartiallySufficient()
    {
        Kingdom kingdom = kingdomBuilder
                .withBuilding(BuildingName.house, 15)
                .build();

        var result = kingdom.passTurn(game.getKingdomInteractor(), weaponsProductionPercentage);

        assertEquals(20, result.turnReport().get().exiledPeople);
    }

    @Test
    void testPeopleLeavingDueToInsufficientHousing_exileProfessionalsWhenHousingIsInsufficient()
    {
        Kingdom kingdom = kingdomBuilder
                .withUnit(UnitName.goldMiner, 5)
                .withUnit(UnitName.blacksmith, 5)
                .withBuilding(BuildingName.house, 5)
                .build();

        var result = kingdom.passTurn(game.getKingdomInteractor(), weaponsProductionPercentage);

        assertThat(result.turnReport().get().exiledPeople).isPositive();
        assertThat(kingdom.getUnits().getTotalCount(UnitName.goldMiner)).isLessThan(5);
        assertThat(kingdom.getUnits().getTotalCount(UnitName.blacksmith)).isLessThan(5);
    }

    @Test
    void whenKingdomHasInssuficientHousing_mobileUnitsShouldNotBeExiled()
    {
        Kingdom kingdom = kingdomBuilder
                .withUnit(UnitName.goldMiner, 5)
                .withUnit(UnitName.blacksmith, 5)
                .withUnit(UnitName.carrier, 5)
                .withUnit(UnitName.bowman, 5)
                .withUnit(UnitName.infantry, 5)
                .withUnit(UnitName.cavalry, 5)
                .withBuilding(BuildingName.house, 0)
                .withBuilding(BuildingName.market, 1)
                .withBuilding(BuildingName.barracks, 5)
                .build();

        kingdom.getUnits().moveAvailableToMobile(UnitName.carrier, 5);
        kingdom.getUnits().moveAvailableToMobile(UnitName.bowman, 5);
        kingdom.getUnits().moveAvailableToMobile(UnitName.infantry, 5);
        kingdom.getUnits().moveAvailableToMobile(UnitName.cavalry, 5);

        kingdom.passTurn(game.getKingdomInteractor(), weaponsProductionPercentage);

        assertEquals(0, kingdom.getUnits().getAvailableCount(UnitName.goldMiner));
        assertEquals(5, kingdom.getUnits().getMobileCount(UnitName.carrier));
        assertEquals(5, kingdom.getUnits().getMobileCount(UnitName.bowman));
        assertEquals(5, kingdom.getUnits().getMobileCount(UnitName.infantry));
        assertEquals(5, kingdom.getUnits().getMobileCount(UnitName.cavalry));
    }
}
