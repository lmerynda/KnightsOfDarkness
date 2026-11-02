package com.knightsofdarkness.web.kingdom.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.web.Game;
import com.knightsofdarkness.web.common.kingdom.BuildingName;
import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.kingdom.SpecialBuildingType;
import com.knightsofdarkness.web.common.kingdom.UnitName;
import com.knightsofdarkness.web.common.kingdom.UnitsMapDto;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.legacy.TestGame;
import com.knightsofdarkness.web.utils.KingdomBuilder;

class KingdomPopulationTest {
    private Game game;
    private GameConfig gameConfig;

    @BeforeEach
    void setUp()
    {
        game = new TestGame().get();
        gameConfig = game.getConfig();
    }

    private KingdomTurnAction turnAction(KingdomEntity kingdom)
    {
        return new KingdomTurnAction(kingdom, game.getKingdomInteractor(), gameConfig, new KingdomDetailsProvider(gameConfig));
    }

    @Test
    void soldiers_shouldLiveInBarracksNotHouses()
    {
        var kingdom = new KingdomBuilder(game).withBuilding(BuildingName.barracks, 10).withUnit(UnitName.cavalry, 500).withResource(ResourceName.food, 100000).build();

        var report = turnAction(kingdom).passTurn(0).data().get();

        assertEquals(0, report.exiledPeople);
        assertEquals(500, kingdom.getUnits().getAvailableCount(UnitName.cavalry));
    }

    @Test
    void allMilitaryUnits_shouldShareBarracksCapacity()
    {
        var kingdom = new KingdomBuilder(game).withBuilding(BuildingName.barracks, 1).withUnit(UnitName.bowman, 60).withUnit(UnitName.infantry, 60).withResource(ResourceName.food, 100000).build();

        var report = turnAction(kingdom).passTurn(0).data().get();

        assertEquals(gameConfig.buildingCapacity().getCapacity(BuildingName.barracks), kingdom.getUnits().countAllMilitary());
        assertEquals(20, report.professionalsLeaving.get(UnitName.bowman) + report.professionalsLeaving.get(UnitName.infantry));
    }

    @Test
    void hunger_shouldMakePeopleLeave()
    {
        var kingdom = new KingdomBuilder(game).withResource(ResourceName.food, 0).withUnit(UnitName.farmer, 0).build();
        var housedPeople = kingdom.getHousedPeopleCount();

        var report = turnAction(kingdom).passTurn(0).data().get();

        assertEquals((int) Math.ceil(housedPeople * gameConfig.common().starvationLeaveRatio()), report.starvedPeople);
    }

    @Test
    void harvest_shouldFeedPeopleEvenFromAnEmptyGranary()
    {
        // the starting ten farmers grow more than the kingdom eats
        var kingdom = new KingdomBuilder(game).withResource(ResourceName.food, 0).build();

        var report = turnAction(kingdom).passTurn(0).data().get();

        assertEquals(1.0, report.nourishmentProductionFactor);
        assertEquals(0, report.starvedPeople);
        assertEquals(report.resourcesProduced.get(ResourceName.food) - report.foodConsumed, kingdom.getResources().getCount(ResourceName.food));
    }

    @Test
    void wellFedKingdom_shouldNotLosePeople()
    {
        var kingdom = new KingdomBuilder(game).withResource(ResourceName.food, 100000).build();

        var report = turnAction(kingdom).passTurn(0).data().get();

        assertEquals(0, report.starvedPeople);
    }

    @Test
    void training_shouldRespectSharedBarracksCapacity()
    {
        var kingdom = new KingdomBuilder(game).withBuilding(BuildingName.barracks, 1).withUnit(UnitName.infantry, 80)
                .withResource(ResourceName.gold, 10000000).withResource(ResourceName.weapons, 10000).withResource(ResourceName.unemployed, 1000).build();
        var toTrain = new UnitsMapDto();
        toTrain.setCount(UnitName.bowman, 50);

        var trained = new KingdomTrainAction(kingdom, gameConfig).train(toTrain).data().orElseGet(UnitsMapDto::new);

        assertEquals(20, trained.getCount(UnitName.bowman));
    }

    @Test
    void specialBuilding_firstLevel_shouldCostConfiguredPoints()
    {
        var kingdom = new KingdomBuilder(game).build();
        var action = new KingdomSpecialBuildingAction(kingdom, gameConfig);

        var granary = action.startSpecialBuilding(SpecialBuildingType.granary).orElseThrow();

        assertEquals(gameConfig.specialBuildingCosts().granary(), granary.buildingPointsRequired);
    }

    @Test
    void specialBuilding_withFewerPointsThanRequested_shouldNotLevelUp()
    {
        var kingdom = new KingdomBuilder(game).withResource(ResourceName.buildingPoints, 100).build();
        var action = new KingdomSpecialBuildingAction(kingdom, gameConfig);
        var shaft = action.startSpecialBuilding(SpecialBuildingType.goldShaft).orElseThrow();

        var spent = action.buildSpecialBuilding(shaft, 1000000);

        assertEquals(100, spent);
        assertEquals(0, shaft.level);
        assertEquals(100, shaft.buildingPointsPut);
        assertEquals(0, kingdom.getResources().getCount(ResourceName.buildingPoints));
    }

    @Test
    void specialBuilding_withoutAType_shouldNotStart()
    {
        var kingdom = new KingdomBuilder(game).build();

        assertThat(new KingdomSpecialBuildingAction(kingdom, gameConfig).startSpecialBuilding(null)).isEmpty();
    }
}
