package com.knightsofdarkness.web.kingdom.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.web.Game;
import com.knightsofdarkness.web.common.kingdom.BuildingName;
import com.knightsofdarkness.web.common.kingdom.KingdomBuildingsDto;
import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.kingdom.UnitName;
import com.knightsofdarkness.web.common.kingdom.UnitsMapDto;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.kingdom.IKingdomInteractor;
import com.knightsofdarkness.web.legacy.TestGame;
import com.knightsofdarkness.web.utils.KingdomBuilder;

class KingdomTurnTest {
    private Game game;
    private KingdomBuilder kingdomBuilder;
    private static final int weaponsProductionPercentage = 0;
    private IKingdomInteractor kingdomInteractor;
    private GameConfig gameConfig;
    private KingdomDetailsProvider kingdomDetailsProvider;

    @BeforeEach
    void setUp()
    {
        game = new TestGame().get();
        kingdomBuilder = new KingdomBuilder(game);
        kingdomInteractor = game.getKingdomInteractor();
        gameConfig = game.getConfig();
        kingdomDetailsProvider = new KingdomDetailsProvider(gameConfig);
    }

    @Test
    void passTurnSanityTest()
    {
        var kingdom = kingdomBuilder.withUnit(UnitName.goldMiner, 0).build();
        Map<ResourceName, Integer> resourcesBeforeTurn = new EnumMap<>(kingdom.getResources().resources);

        var action = new KingdomTurnAction(kingdom, kingdomInteractor, gameConfig, kingdomDetailsProvider);
        action.passTurn(weaponsProductionPercentage);

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

        var action = new KingdomTurnAction(kingdom, kingdomInteractor, gameConfig, kingdomDetailsProvider);
        action.passTurn(weaponsProductionPercentage);

        var goldMinerProductionRate = gameConfig.production().getProductionRate(UnitName.goldMiner);
        var newProduction = goldMinersCount * goldMinerProductionRate;
        assertEquals(resourcesBeforeTurn.get(ResourceName.gold) + newProduction, kingdom.getResources().getCount(ResourceName.gold));
    }

    @Test
    void passTurn_withEnoughIron_thenBlacksmithsShouldProduceToolsAtFullRate()
    {
        var blacksmithCount = 10;
        var blacksmithProductionRate = gameConfig.production().getProductionRate(UnitName.blacksmith);
        var kingdom = kingdomBuilder
                .withUnit(UnitName.blacksmith, blacksmithCount)
                .withLand(1000) // to avoid small kingdom bonus
                .withBuilding(BuildingName.workshop, 1000)
                .build();
        kingdom.getResources().setCount(ResourceName.iron, kingdomDetailsProvider.getIronUpkeep(kingdom, 1.0));
        Map<ResourceName, Integer> resourcesBeforeTurn = new EnumMap<>(kingdom.getResources().resources);

        var action = new KingdomTurnAction(kingdom, kingdomInteractor, gameConfig, kingdomDetailsProvider);
        action.passTurn(weaponsProductionPercentage);

        var newProduction = blacksmithCount * blacksmithProductionRate;

        assertEquals(resourcesBeforeTurn.get(ResourceName.tools) + newProduction, kingdom.getResources().getCount(ResourceName.tools));
    }

    @Test
    void passTurn_withEnoughIronAndNoIronProduction_shouldConsumeExactAmountOfIron()
    {
        var blacksmithCount = 10;
        var kingdom = kingdomBuilder.withUnit(UnitName.blacksmith, blacksmithCount).withUnit(UnitName.ironMiner, 0).build();
        var ironUpkeep = kingdomDetailsProvider.getIronUpkeep(kingdom, 1.0);
        kingdom.getResources().setCount(ResourceName.iron, ironUpkeep);
        Map<ResourceName, Integer> resourcesBeforeTurn = new EnumMap<>(kingdom.getResources().resources);

        var action = new KingdomTurnAction(kingdom, kingdomInteractor, gameConfig, kingdomDetailsProvider);
        action.passTurn(weaponsProductionPercentage);

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

        var ironUpkeep = kingdomDetailsProvider.getIronUpkeep(kingdom, 1.0);
        kingdom.getResources().setCount(ResourceName.iron, ironUpkeep - 1);

        var action = new KingdomTurnAction(kingdom, kingdomInteractor, gameConfig, kingdomDetailsProvider);
        action.passTurn(weaponsProductionPercentage);

        assertEquals(0, kingdom.getResources().getCount(ResourceName.iron));
    }

    @Test
    void passTurn_withNotEnoughIronAndNoIronProduction_shouldProduceToolsAtProportionallySmallerRate()
    {
        var blacksmithCount = 10;
        var blacksmithProductionRate = gameConfig.production().getProductionRate(UnitName.blacksmith);
        var kingdom = kingdomBuilder
                .withUnit(UnitName.blacksmith, blacksmithCount)
                .withUnit(UnitName.ironMiner, 0)
                .withLand(1000) // to avoid small kingdom bonus
                .withBuilding(BuildingName.workshop, 1000)
                .build();

        var ironUpkeep = kingdomDetailsProvider.getIronUpkeep(kingdom, 1.0);
        kingdom.getResources().setCount(ResourceName.iron, ironUpkeep / 2);
        Map<ResourceName, Integer> resourcesBeforeTurn = new EnumMap<>(kingdom.getResources().resources);

        var action = new KingdomTurnAction(kingdom, kingdomInteractor, gameConfig, kingdomDetailsProvider);
        action.passTurn(weaponsProductionPercentage);

        var newProduction = blacksmithCount * blacksmithProductionRate / 2;

        assertEquals(resourcesBeforeTurn.get(ResourceName.tools) + newProduction, kingdom.getResources().getCount(ResourceName.tools));
    }

    @Test
    void passTurn_withZeroIronAndSufficientIronProductionForBlacksmiths_shouldProduceToolsAtFullRate()
    {
        var blacksmithCount = 10;
        var blacksmithProductionRate = gameConfig.production().getProductionRate(UnitName.blacksmith);
        var ironMinerProductionRate = gameConfig.production().getProductionRate(UnitName.ironMiner);
        var ironToSpendPerToolProduction = 1;
        var ironMinerCount = blacksmithCount * blacksmithProductionRate / ironMinerProductionRate * ironToSpendPerToolProduction;
        var kingdom = kingdomBuilder
                .withUnit(UnitName.blacksmith, blacksmithCount)
                .withUnit(UnitName.ironMiner, ironMinerCount)
                .withLand(1000) // to avoid small kingdom bonus
                .withBuilding(BuildingName.workshop, 1000)
                .build();
        Map<ResourceName, Integer> resourcesBeforeTurn = new EnumMap<>(kingdom.getResources().resources);

        var action = new KingdomTurnAction(kingdom, kingdomInteractor, gameConfig, kingdomDetailsProvider);
        action.passTurn(weaponsProductionPercentage);

        var newProduction = blacksmithCount * blacksmithProductionRate;

        assertEquals(resourcesBeforeTurn.get(ResourceName.tools) + newProduction, kingdom.getResources().getCount(ResourceName.tools));
    }

    @Test
    void passTurn_withZeroIronAndSufficientIronProductionForBlacksmiths_shouldRemainWithZeroIron()
    {
        var blacksmithCount = 10;
        var blacksmithProductionRate = gameConfig.production().getProductionRate(UnitName.blacksmith);
        var ironMinerProductionRate = gameConfig.production().getProductionRate(UnitName.ironMiner);
        var ironToSpendPerToolProduction = 1;
        var ironMinerCount = blacksmithCount * blacksmithProductionRate / ironMinerProductionRate * ironToSpendPerToolProduction;
        var kingdom = kingdomBuilder
                .withUnit(UnitName.blacksmith, blacksmithCount)
                .withUnit(UnitName.ironMiner, ironMinerCount)
                .withResource(ResourceName.iron, 0)
                .withLand(1000) // to avoid small kingdom bonus
                .withBuilding(BuildingName.workshop, 1000)
                .build();

        var action = new KingdomTurnAction(kingdom, kingdomInteractor, gameConfig, kingdomDetailsProvider);
        action.passTurn(weaponsProductionPercentage);

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
        var action = new KingdomTurnAction(kingdom, kingdomInteractor, gameConfig, kingdomDetailsProvider);
        action.passTurn(weaponsProductionPercentage);
        Map<ResourceName, Integer> resourcesBeforeTurn = new EnumMap<>(kingdom.getResources().resources);
        // count food for upkeep
        var foodUpkeep = kingdomDetailsProvider.getFoodUpkeep(kingdom);
        resourcesBeforeTurn.put(ResourceName.food, resourcesBeforeTurn.get(ResourceName.food) - foodUpkeep);
        // count iron for upkeep
        var ironUpkeep = kingdomDetailsProvider.getIronUpkeep(kingdom, 1.0);
        resourcesBeforeTurn.put(ResourceName.iron, resourcesBeforeTurn.get(ResourceName.iron) - ironUpkeep);
        // building points will just refresh on new turn, remove them by one to make sureafter new turn we have more
        resourcesBeforeTurn.put(ResourceName.buildingPoints, resourcesBeforeTurn.get(ResourceName.buildingPoints) - 1);
        action.passTurn(weaponsProductionPercentage);

        for (var unitName : UnitName.getProductionUnits())
        {
            var resourceName = gameConfig.production().getResource(unitName);
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

        var action = new KingdomTurnAction(kingdom, kingdomInteractor, gameConfig, kingdomDetailsProvider);
        action.passTurn(weaponsProductionPercentage);

        var beforeTurn = resourcesBeforeTurn.get(ResourceName.unemployed);
        var afterTurn = kingdom.getResources().getCount(ResourceName.unemployed);
        assertTrue(beforeTurn < afterTurn, "Expected number of unemployed before turn " + beforeTurn + " to be lower than after turn " + afterTurn);
    }

    @Test
    void passTurn_WithFullHousingCapacity_NewPeopleShouldNotArriveTest()
    {
        var kingdom = kingdomBuilder.withResource(ResourceName.unemployed, 9999).build();
        Map<ResourceName, Integer> resourcesBeforeTurn = new EnumMap<>(kingdom.getResources().resources);

        var action = new KingdomTurnAction(kingdom, kingdomInteractor, gameConfig, kingdomDetailsProvider);
        action.passTurn(weaponsProductionPercentage);

        var beforeTurn = resourcesBeforeTurn.get(ResourceName.unemployed);
        var afterTurn = kingdom.getResources().getCount(ResourceName.unemployed);
        assertTrue(beforeTurn >= afterTurn, "Expected number of unemployed before turn " + beforeTurn + " to NOT be lower than after turn " + afterTurn);
    }

    @Test
    void passTurn_withInsufficientHousing_shouldSendPeopleToExile()
    {
        var kingdom = kingdomBuilder.build();
        var action = new KingdomTurnAction(kingdom, kingdomInteractor, gameConfig, kingdomDetailsProvider);
        action.passTurn(weaponsProductionPercentage);

        var beforeTurn = kingdom.getResources().getCount(ResourceName.unemployed);
        kingdom.getBuildings().subtractCount(BuildingName.house, 1);
        action.passTurn(weaponsProductionPercentage);

        var afterTurn = kingdom.getResources().getCount(ResourceName.unemployed);
        assertTrue(beforeTurn > afterTurn, "Expected number of unemployed to reduce due to insufficient housing");
    }

    @Test
    void passMoreTurnsThanMaximumTest()
    {
        var kingdom = kingdomBuilder.build();
        var numberOfTurns = kingdom.getResources().getCount(ResourceName.turns);
        var action = new KingdomTurnAction(kingdom, kingdomInteractor, gameConfig, kingdomDetailsProvider);

        // first we pass every turn that kingdom has
        for (int i = 0; i < numberOfTurns; i++)
        {
            assertTrue(action.passTurn(weaponsProductionPercentage).success());
        }

        // then we try to pass turn which is not available
        assertFalse(action.passTurn(weaponsProductionPercentage).success());
    }

    @Disabled("Currently KingdomService holds the addTurn check, revisit this test once the logic is moved away")
    @Test
    void whenTurnCountBelowMaximum_addingTurn_shouldResultInMoreTurnsAvailable()
    {
        var kingdom = kingdomBuilder.build();
        var numberOfTurns = kingdom.getResources().getCount(ResourceName.turns);

        kingdom.addTurn();

        assertEquals(numberOfTurns + 1, kingdom.getResources().getCount(ResourceName.turns));
    }

    @Disabled("Currently KingdomService holds the addTurn check, revisit this test once the logic is moved away")
    @Test
    void whenTurnCountReachesMaximum_addingTurn_shouldNotChangeTurnCount()
    {
        var kingdom = kingdomBuilder.withResource(ResourceName.turns, gameConfig.common().maxTurns()).build();
        var numberOfTurns = kingdom.getResources().getCount(ResourceName.turns);

        kingdom.addTurn();

        assertEquals(numberOfTurns, kingdom.getResources().getCount(ResourceName.turns));
    }

    @Test
    void passingTurn_shouldSaveReport()
    {
        var kingdom = kingdomBuilder.build();
        var action = new KingdomTurnAction(kingdom, kingdomInteractor, gameConfig, kingdomDetailsProvider);
        var result = action.passTurn(weaponsProductionPercentage);
        assertTrue(result.success());

        var savedReport = kingdom.getLastTurnReport();

        assertEquals(result.turnReport().get(), savedReport);
    }

    @Test
    void passTurn_withInsufficientProfessionalBuildingCapacity_shouldFireExceedingNumber()
    {
        var kingdom = kingdomBuilder.build();
        var action = new KingdomTurnAction(kingdom, kingdomInteractor, gameConfig, kingdomDetailsProvider);
        action.passTurn(weaponsProductionPercentage);

        trainGoldMinersToReachMaxCapacity(kingdom);

        var unemployedBeforeTurn = kingdom.getResources().getCount(ResourceName.unemployed);
        var goldMinersBeforeTurnCount = kingdom.getUnits().getTotalCount(UnitName.goldMiner);
        var toDemolish = new KingdomBuildingsDto();
        toDemolish.setCount(BuildingName.goldMine, 1);
        var demolishAction = new KingdomBuildAction(kingdom, gameConfig);
        demolishAction.demolish(toDemolish);
        action.passTurn(weaponsProductionPercentage);

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
        var action = new KingdomTurnAction(kingdom, kingdomInteractor, gameConfig, kingdomDetailsProvider);
        action.passTurn(weaponsProductionPercentage);

        var unemployedBeforeTurn = kingdom.getResources().getCount(ResourceName.unemployed);
        var toDemolish = new KingdomBuildingsDto();
        toDemolish.setCount(BuildingName.house, 1);
        var demolishAction = new KingdomBuildAction(kingdom, gameConfig);
        demolishAction.demolish(toDemolish);

        action.passTurn(weaponsProductionPercentage);

        var unemployedAfterTurn = kingdom.getResources().getCount(ResourceName.unemployed);
        assertThat(unemployedAfterTurn).isLessThan(unemployedBeforeTurn);
    }

    private void trainGoldMinersToReachMaxCapacity(KingdomEntity kingdom)
    {
        var currentGoldMinersCount = kingdom.getUnits().getTotalCount(UnitName.goldMiner);
        var goldMinersCapacity = kingdomDetailsProvider.getBuildingCapacity(kingdom, BuildingName.goldMine);
        var unitsToTrain = new UnitsMapDto();
        unitsToTrain.setCount(UnitName.goldMiner, goldMinersCapacity - currentGoldMinersCount);
        var trainAction = new KingdomTrainAction(kingdom, gameConfig);
        trainAction.train(unitsToTrain);
        // TODO why are we passing turn here?
        var action = new KingdomTurnAction(kingdom, kingdomInteractor, gameConfig, kingdomDetailsProvider);
        action.passTurn(weaponsProductionPercentage);
    }

    @Test
    void testPeopleLeavingDueToInsufficientHousing_noExileWhenHousingIsSufficient()
    {
        var kingdom = kingdomBuilder
                .withUnit(UnitName.goldMiner, 10)
                .withBuilding(BuildingName.house, 25)
                .build();

        var action = new KingdomTurnAction(kingdom, kingdomInteractor, gameConfig, kingdomDetailsProvider);
        var result = action.passTurn(weaponsProductionPercentage);

        assertEquals(0, result.turnReport().get().exiledPeople);
    }

    @Test
    void testPeopleLeavingDueToInsufficientHousing_exileWhenHousingIsInsufficient()
    {
        var kingdom = kingdomBuilder
                .withBuilding(BuildingName.house, 1)
                .build();

        var action = new KingdomTurnAction(kingdom, kingdomInteractor, gameConfig, kingdomDetailsProvider);
        var result = action.passTurn(weaponsProductionPercentage);

        assertEquals(0, kingdom.getResources().getCount(ResourceName.unemployed));
        assertThat(result.turnReport().get().exiledPeople).isPositive();
    }

    @Test
    void testPeopleLeavingDueToInsufficientHousing_partialExileWhenHousingIsPartiallySufficient()
    {
        var kingdom = kingdomBuilder
                .withBuilding(BuildingName.house, 9)
                .build();

        var action = new KingdomTurnAction(kingdom, kingdomInteractor, gameConfig, kingdomDetailsProvider);
        var result = action.passTurn(weaponsProductionPercentage);

        assertEquals(20, result.turnReport().get().exiledPeople);
    }

    @Test
    void testPeopleLeavingDueToInsufficientHousing_exileProfessionalsWhenHousingIsInsufficient()
    {
        var kingdom = kingdomBuilder
                .withUnit(UnitName.goldMiner, 5)
                .withUnit(UnitName.blacksmith, 5)
                .withBuilding(BuildingName.house, 0)
                .build();

        var action = new KingdomTurnAction(kingdom, kingdomInteractor, gameConfig, kingdomDetailsProvider);
        var result = action.passTurn(weaponsProductionPercentage);

        assertThat(result.turnReport().get().exiledPeople).isPositive();
        assertThat(kingdom.getUnits().getTotalCount(UnitName.goldMiner)).isLessThan(5);
        assertThat(kingdom.getUnits().getTotalCount(UnitName.blacksmith)).isLessThan(5);
    }

    @Test
    void whenKingdomHasInssuficientHousing_mobileUnitsShouldNotBeExiled()
    {
        var kingdom = kingdomBuilder
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

        var action = new KingdomTurnAction(kingdom, kingdomInteractor, gameConfig, kingdomDetailsProvider);
        action.passTurn(weaponsProductionPercentage);

        assertThat(kingdom.getUnits().getAvailableCount(UnitName.goldMiner)).isLessThan(5);
        assertEquals(5, kingdom.getUnits().getMobileCount(UnitName.carrier));
        assertEquals(5, kingdom.getUnits().getMobileCount(UnitName.bowman));
        assertEquals(5, kingdom.getUnits().getMobileCount(UnitName.infantry));
        assertEquals(5, kingdom.getUnits().getMobileCount(UnitName.cavalry));
    }
}
