package com.merynda.kod.kingdom;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.merynda.kod.TestGame;
import com.merynda.kod.game.Game;
import com.merynda.kod.gameconfig.GameConfig;
import com.merynda.kod.utils.KingdomBuilder;

class KingdomBuildTest {
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
    void buildSanityTest()
    {
        var kingdom = kingdomBuilder.build();
        var buildingPointsBeforeBuild = kingdom.getResources().getCount(ResourceName.buildingPoints);
        var toBuild = new KingdomBuildings();

        kingdom.build(toBuild);

        assertEquals(buildingPointsBeforeBuild, kingdom.getResources().getCount(ResourceName.buildingPoints));
    }

    @Test
    void buildHousesAndGoldMinesTest()
    {
        var kingdom = kingdomBuilder.build();
        var housesBeforeBuild = kingdom.getBuildings().getCount(BuildingName.house);
        var goldMinesBeforeBuild = kingdom.getBuildings().getCount(BuildingName.goldMine);
        var buildingPointsBeforeBuild = kingdom.getResources().getCount(ResourceName.buildingPoints);

        var housesToBuild = 5;
        var goldMinesToBuild = 2;
        var toBuild = new KingdomBuildings();
        toBuild.addCount(BuildingName.house, housesToBuild);
        toBuild.addCount(BuildingName.goldMine, goldMinesToBuild);

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
        var kingdom = kingdomBuilder.build();
        var unusedLandBeforeBuild = kingdom.getUnusedLand();

        var housesToBuild = 5;
        var toBuild = new KingdomBuildings();
        toBuild.addCount(BuildingName.house, housesToBuild);

        kingdom.build(toBuild);

        assertEquals(unusedLandBeforeBuild - housesToBuild, kingdom.getUnusedLand());
    }

    @Test
    void buildingCount()
    {
        var kingdom = kingdomBuilder.build();
        var existingBuildingsCount = kingdom.getBuildings().countAll();

        var toBuild = new KingdomBuildings();
        toBuild.addCount(BuildingName.house, 1);
        toBuild.addCount(BuildingName.farm, 3);
        toBuild.addCount(BuildingName.goldMine, 2);
        toBuild.addCount(BuildingName.workshop, 1);

        kingdom.build(toBuild);

        assertEquals(existingBuildingsCount + 7, kingdom.getBuildings().countAll());
    }

    @Test
    void whenAllTheLandIsUsed_shouldNotBeAbleToBuild()
    {
        var kingdom = kingdomBuilder.withResource(ResourceName.land, 13000).build();
        var buildingsCountBeforeBuild = kingdom.getBuildings().countAll();
        var toBuild = new KingdomBuildings();
        toBuild.addCount(BuildingName.house, 1);

        kingdom.build(toBuild);

        assertEquals(buildingsCountBeforeBuild, kingdom.getBuildings().countAll());
    }
}