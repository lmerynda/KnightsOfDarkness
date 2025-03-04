package com.knightsofdarkness.web.kingdom.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.common.kingdom.BuildingName;
import com.knightsofdarkness.common.kingdom.KingdomBuildingsDto;
import com.knightsofdarkness.common.kingdom.ResourceName;
import com.knightsofdarkness.web.Game;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.legacy.TestGame;
import com.knightsofdarkness.web.utils.KingdomBuilder;

class KingdomBuildTest {
    private Game game;
    private GameConfig config;
    private KingdomBuilder kingdomBuilder;
    private KingdomEntity kingdom;

    @BeforeEach
    void setUp()
    {
        game = new TestGame().get();
        config = game.getConfig();
        kingdomBuilder = new KingdomBuilder(game);
        kingdom = kingdomBuilder.withRichConfiguration().build();
        game.addKingdom(kingdom);
    }

    @Test
    void buildSanityTest()
    {
        var buildingPointsBeforeBuild = kingdom.getResources().getCount(ResourceName.buildingPoints);
        var action = new KingdomBuildAction(kingdom, config);
        action.build(new KingdomBuildingsDto());

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

        var action = new KingdomBuildAction(kingdom, config);
        var result = action.build(toBuild);

        assertEquals(5, result.buildings().getCount(BuildingName.house));
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

        var action = new KingdomBuildAction(kingdom, config);
        action.build(toBuild);

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

        var action = new KingdomBuildAction(kingdom, config);
        action.build(toBuild);

        assertEquals(existingBuildingsCount + 7, kingdom.getBuildings().countAll());
    }

    @Test
    void whenAllTheLandIsUsed_shouldNotBeAbleToBuild()
    {
        var buildingsCountBeforeBuild = kingdom.getBuildings().countAll();
        kingdom.getResources().setCount(ResourceName.land, buildingsCountBeforeBuild);
        var toBuild = new KingdomBuildingsDto();
        toBuild.setCount(BuildingName.house, 1);

        var action = new KingdomBuildAction(kingdom, config);
        action.build(toBuild);

        assertEquals(buildingsCountBeforeBuild, kingdom.getBuildings().countAll());
    }

    @Test
    void demolishHouseTest()
    {
        var housesBeforeDemolish = kingdom.getBuildings().getCount(BuildingName.house);

        var toDemolish = new KingdomBuildingsDto();
        toDemolish.setCount(BuildingName.house, 1);

        var action = new KingdomBuildAction(kingdom, config);
        action.demolish(toDemolish);

        assertEquals(housesBeforeDemolish - 1, kingdom.getBuildings().getCount(BuildingName.house));
    }
}
