package com.knightsofdarkness.web.kingdom.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.web.Game;
import com.knightsofdarkness.web.common.kingdom.AttackType;
import com.knightsofdarkness.web.common.kingdom.SendAttackDto;
import com.knightsofdarkness.web.common.kingdom.UnitName;
import com.knightsofdarkness.web.common.kingdom.UnitsMapDto;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.legacy.TestGame;
import com.knightsofdarkness.web.utils.KingdomBuilder;

class KingdomMilitaryActionTest {
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
    void whenKingdomHasNoInfantry_sendingAttack_shouldResultInFailure()
    {
        kingdom.getUnits().setCount(UnitName.infantry, 0);
        kingdom.getUnits().setCount(UnitName.bowman, 0);
        kingdom.getUnits().setCount(UnitName.cavalry, 0);

        var units = new UnitsMapDto();
        units.setCount(UnitName.infantry, 100);

        var sendAttackDto = new SendAttackDto("destination", AttackType.economy, units);
        var action = new KingdomMilitaryAction(kingdom, gameConfig);
        var sendAttackResult = action.sendAttack(sendAttackDto);

        assertFalse(sendAttackResult.success());
    }

    @Test
    void whenKingdomHasNoBowman_sendingAttack_shouldResultInFailure()
    {
        kingdom.getUnits().setCount(UnitName.infantry, 0);
        kingdom.getUnits().setCount(UnitName.bowman, 0);
        kingdom.getUnits().setCount(UnitName.cavalry, 0);

        var units = new UnitsMapDto();
        units.setCount(UnitName.bowman, 100);

        var sendAttackDto = new SendAttackDto("destination", AttackType.economy, units);
        var action = new KingdomMilitaryAction(kingdom, gameConfig);
        var sendAttackResult = action.sendAttack(sendAttackDto);

        assertFalse(sendAttackResult.success());
    }

    @Test
    void whenKingdomHasNoCavalry_sendingAttack_shouldResultInFailure()
    {
        kingdom.getUnits().setCount(UnitName.infantry, 0);
        kingdom.getUnits().setCount(UnitName.bowman, 0);
        kingdom.getUnits().setCount(UnitName.cavalry, 0);

        var units = new UnitsMapDto();
        units.setCount(UnitName.cavalry, 100);

        var sendAttackDto = new SendAttackDto("destination", AttackType.economy, units);
        var action = new KingdomMilitaryAction(kingdom, gameConfig);
        var sendAttackResult = action.sendAttack(sendAttackDto);

        assertFalse(sendAttackResult.success());
    }

    @Test
    void whenKingdomHasNoUnits_sendingAttack_shouldResultInFailure()
    {
        kingdom.getUnits().setCount(UnitName.infantry, 0);
        kingdom.getUnits().setCount(UnitName.bowman, 0);
        kingdom.getUnits().setCount(UnitName.cavalry, 0);

        var units = new UnitsMapDto();
        units.setCount(UnitName.infantry, 50);
        units.setCount(UnitName.bowman, 50);
        units.setCount(UnitName.cavalry, 50);

        var sendAttackDto = new SendAttackDto("destination", AttackType.economy, units);
        var action = new KingdomMilitaryAction(kingdom, gameConfig);
        var sendAttackResult = action.sendAttack(sendAttackDto);

        assertFalse(sendAttackResult.success());
    }

    @Test
    void whenKingdomHasEnoughUnits_sendingAttack_shouldResultInSuccess()
    {
        kingdom.getUnits().setCount(UnitName.infantry, 100);
        kingdom.getUnits().setCount(UnitName.bowman, 100);
        kingdom.getUnits().setCount(UnitName.cavalry, 100);

        var units = new UnitsMapDto();
        units.setCount(UnitName.infantry, 50);
        units.setCount(UnitName.bowman, 50);
        units.setCount(UnitName.cavalry, 50);

        var sendAttackDto = new SendAttackDto("destination", AttackType.economy, units);
        var action = new KingdomMilitaryAction(kingdom, gameConfig);
        var sendAttackResult = action.sendAttack(sendAttackDto);

        assertTrue(sendAttackResult.success());
    }

    @Test
    void whenKingdomHasEnoughUnits_sendingAttack_shouldMoveUnitsFromAvailableToMobile()
    {
        kingdom.getUnits().setCount(UnitName.infantry, 100);
        kingdom.getUnits().setCount(UnitName.bowman, 100);
        kingdom.getUnits().setCount(UnitName.cavalry, 100);

        var units = new UnitsMapDto();
        units.setCount(UnitName.infantry, 10);
        units.setCount(UnitName.bowman, 10);
        units.setCount(UnitName.cavalry, 10);

        var sendAttackDto = new SendAttackDto("destination", AttackType.economy, units);
        var action = new KingdomMilitaryAction(kingdom, gameConfig);
        action.sendAttack(sendAttackDto);

        assertEquals(90, kingdom.getUnits().getAvailableCount(UnitName.infantry));
        assertEquals(90, kingdom.getUnits().getAvailableCount(UnitName.bowman));
        assertEquals(90, kingdom.getUnits().getAvailableCount(UnitName.cavalry));

        assertEquals(10, kingdom.getUnits().getMobileCount(UnitName.infantry));
        assertEquals(10, kingdom.getUnits().getMobileCount(UnitName.bowman));
        assertEquals(10, kingdom.getUnits().getMobileCount(UnitName.cavalry));
    }
}
