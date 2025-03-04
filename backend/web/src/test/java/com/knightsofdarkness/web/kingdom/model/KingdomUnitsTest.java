package com.knightsofdarkness.web.kingdom.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.common.kingdom.UnitName;
import com.knightsofdarkness.web.Game;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.legacy.TestGame;
import com.knightsofdarkness.web.utils.KingdomBuilder;

class KingdomUnitsTest {
    private Game game;
    private GameConfig gameConfig;
    private KingdomBuilder kingdomBuilder;
    private KingdomEntity kingdom;

    @BeforeEach
    void setUp()
    {
        game = new TestGame().get();
        gameConfig = game.getConfig();
        kingdomBuilder = new KingdomBuilder(game);
        kingdom = kingdomBuilder.withRichConfiguration().build();
        game.addKingdom(kingdom);
    }

    @Test
    void getTotalCountTest()
    {
        var kingdom = kingdomBuilder.build();
        var units = kingdom.getUnits();
        var initialCount = units.countAll();
        units.addCount(UnitName.goldMiner, 10);

        assertEquals(initialCount + 10, units.countAll());
    }

    @Test
    void getTotalCountTest2()
    {
        var kingdom = kingdomBuilder.build();
        var units = kingdom.getUnits();
        var initialCount = units.countAll();
        units.addCount(UnitName.goldMiner, 10);
        units.addCount(UnitName.ironMiner, 10);
        units.addCount(UnitName.cavalry, 10);

        assertEquals(initialCount + 30, units.countAll());
    }

    @Test
    void getUnitsRatiosTestWithSingleUnit()
    {
        var kingdom = kingdomBuilder.build();
        var units = kingdom.getUnits();
        for (var unit : UnitName.values())
        {
            units.setCount(unit, 0);
        }
        units.setCount(UnitName.goldMiner, 10);
        var unitsRatios = units.getUnitsRatios();

        assertEquals(1.0, unitsRatios.get(UnitName.goldMiner));
    }

    @Test
    void getUnitsRatiosTestWithMultipleUnits()
    {
        var kingdom = kingdomBuilder.build();
        var units = kingdom.getUnits();
        for (var unit : UnitName.values())
        {
            units.setCount(unit, 0);
        }
        units.setCount(UnitName.goldMiner, 10);
        units.setCount(UnitName.ironMiner, 10);
        units.setCount(UnitName.blacksmith, 10);
        units.setCount(UnitName.farmer, 10);
        var unitsRatios = units.getUnitsRatios();

        assertEquals(0.25, unitsRatios.get(UnitName.goldMiner));
        assertEquals(0.25, unitsRatios.get(UnitName.ironMiner));
        assertEquals(0.25, unitsRatios.get(UnitName.blacksmith));
        assertEquals(0.25, unitsRatios.get(UnitName.farmer));
    }
}
