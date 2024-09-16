package com.knightsofdarkness.game.kingdom;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.common.kingdom.BuildingName;
import com.knightsofdarkness.common.kingdom.KingdomBuildingsDto;
import com.knightsofdarkness.common.kingdom.ResourceName;
import com.knightsofdarkness.game.Game;
import com.knightsofdarkness.game.TestGame;
import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.game.utils.KingdomBuilder;

class KingdomBuildTest {
    private static Game game;
    private static GameConfig config;
    private KingdomBuilder kingdomBuilder;
    private Kingdom kingdom;

    @BeforeAll
    static void beforeAll()
    {
        game = new TestGame().get();
        config = game.getConfig();
    }

    @BeforeEach
    void setUp()
    {
        kingdomBuilder = new KingdomBuilder(game);
        kingdom = kingdomBuilder.build();
    }

    @Test
    void buildSanityTest()
    {
        var buildingPointsBeforeBuild = kingdom.getResources().getCount(ResourceName.buildingPoints);
        var toBuild = new KingdomBuildingsDto();

        kingdom.build(toBuild);

        assertEquals(buildingPointsBeforeBuild, kingdom.getResources().getCount(ResourceName.buildingPoints));
    }

    @Test
    void buildHousesAndGoldMinesTest()
    {
        var housesBeforeBuild = kingdom.getBuildings().getCount(BuildingName.house);
        var goldMinesBeforeBuild = kingdom.getBuildings().getCount(BuildingName.goldMine);
        var buildingPointsBeforeBuild = kingdom.getResources().getCount(ResourceName.buildingPoints);

        var housesToBuild = 5;
        var goldMinesToBuild = 2;
        var toBuild = new KingdomBuildingsDto();
        toBuild.setCount(BuildingName.house, housesToBuild);
        toBuild.setCount(BuildingName.goldMine, goldMinesToBuild);

        kingdom.build(toBuild);

        assertEquals(housesBeforeBuild + 5, kingdom.getBuildings().getCount(BuildingName.house));
        assertEquals(goldMinesBeforeBuild + 2, kingdom.getBuildings().getCount(BuildingName.goldMine));

        var housesCost = housesToBuild * config.buildingPointCosts().house();
        var goldMinesCost = goldMinesToBuild * config.buildingPointCosts().goldMine();
        var totalCost = housesCost + goldMinesCost;

        assertEquals(buildingPointsBeforeBuild - totalCost, kingdom.getResources().getCount(ResourceName.buildingPoints));
    }

    @Test
    void whenBuildingFiveBuildings_KingdomShouldHaveFiveLessUnusedLand()
    {
        var unusedLandBeforeBuild = kingdom.getUnusedLand();

        var housesToBuild = 5;
        var toBuild = new KingdomBuildingsDto();
        toBuild.setCount(BuildingName.house, housesToBuild);

        kingdom.build(toBuild);

        assertEquals(unusedLandBeforeBuild - housesToBuild, kingdom.getUnusedLand());
    }

    @Test
    void buildingCount()
    {
        var existingBuildingsCount = kingdom.getBuildings().countAll();

        var toBuild = new KingdomBuildingsDto();
        toBuild.setCount(BuildingName.house, 1);
        toBuild.setCount(BuildingName.farm, 3);
        toBuild.setCount(BuildingName.goldMine, 2);
        toBuild.setCount(BuildingName.workshop, 1);

        kingdom.build(toBuild);

        assertEquals(existingBuildingsCount + 7, kingdom.getBuildings().countAll());
    }

    @Test
    void whenAllTheLandIsUsed_shouldNotBeAbleToBuild()
    {
        var buildingsCountBeforeBuild = kingdom.getBuildings().countAll();
        kingdom.getResources().setCount(ResourceName.land, buildingsCountBeforeBuild);
        var toBuild = new KingdomBuildingsDto();
        toBuild.setCount(BuildingName.house, 1);

        kingdom.build(toBuild);

        assertEquals(buildingsCountBeforeBuild, kingdom.getBuildings().countAll());
    }

    @Test
    void demolishHouseTest()
    {
        var housesBeforeDemolish = kingdom.getBuildings().getCount(BuildingName.house);

        var toDemolish = new KingdomBuildingsDto();
        toDemolish.setCount(BuildingName.house, 1);

        kingdom.demolish(toDemolish);

        assertEquals(housesBeforeDemolish - 1, kingdom.getBuildings().getCount(BuildingName.house));
    }
}
