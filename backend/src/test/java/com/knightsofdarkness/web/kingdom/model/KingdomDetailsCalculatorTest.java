package com.knightsofdarkness.web.kingdom.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.web.Game;
import com.knightsofdarkness.web.common.kingdom.BuildingName;
import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.kingdom.UnitName;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.legacy.TestGame;
import com.knightsofdarkness.web.utils.KingdomBuilder;

class KingdomDetailsCalculatorTest {
    private Game game;
    private GameConfig gameConfig;

    @BeforeEach
    void setUp()
    {
        game = new TestGame().get();
        gameConfig = game.getConfig();
    }

    @Test
    void details_shouldDescribeStartingKingdom()
    {
        var kingdom = new KingdomBuilder(game).build();

        var details = new KingdomDetailsCalculator(gameConfig).calculate(kingdom, 0);

        assertEquals(59, details.usedLand());
        assertEquals(30 + 30 * 5, details.housingCapacity());
        assertEquals(95, details.housedPeople());
        assertEquals(10 * 5, details.buildingCapacity().get(BuildingName.goldMine));
        assertEquals(30, details.buildingOccupancy().get(BuildingName.goldMine));
        assertEquals(1020, details.nextLandPrice());
        assertEquals(85 * gameConfig.common().foodUpkeepPerUnit(), details.foodUpkeep());
    }

    @Test
    void forecast_shouldMatchTheRealTurnWithoutChangingTheKingdom()
    {
        var kingdom = new KingdomBuilder(game).withUnit(UnitName.blacksmith, 10).build();
        var goldBefore = kingdom.getResources().getCount(ResourceName.gold);
        var turnsBefore = kingdom.getResources().getCount(ResourceName.turns);

        var forecast = new KingdomDetailsCalculator(gameConfig).calculate(kingdom, 30).forecast();

        assertEquals(goldBefore, kingdom.getResources().getCount(ResourceName.gold));
        assertEquals(turnsBefore, kingdom.getResources().getCount(ResourceName.turns));

        var actual = new KingdomTurnAction(kingdom, game.getKingdomInteractor(), gameConfig, new KingdomDetailsProvider(gameConfig)).passTurn(30).data().get();
        assertEquals(actual.resourcesProduced, forecast.resourcesProduced);
        assertEquals(actual.foodConsumed, forecast.foodConsumed);
        assertEquals(actual.arrivingPeople, forecast.arrivingPeople);
    }
}
