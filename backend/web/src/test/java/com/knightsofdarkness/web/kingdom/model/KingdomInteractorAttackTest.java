package com.knightsofdarkness.web.kingdom.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

class KingdomInteractorAttackTest {
    private Game game;
    private GameConfig gameConfig;
    private KingdomEntity primaryKingdom;
    private KingdomEntity secondaryKingdom;

    @BeforeEach
    void setUp()
    {
        game = new TestGame().get();
        gameConfig = game.getConfig();
        primaryKingdom = new KingdomBuilder(game).withName("primary")
                .withUnit(UnitName.bowman, 1000)
                .withUnit(UnitName.infantry, 1000)
                .withUnit(UnitName.cavalry, 1000).build();
        game.addKingdom(primaryKingdom);
        secondaryKingdom = new KingdomBuilder(game).withName("secondary")
                .withUnit(UnitName.bowman, 1000)
                .withUnit(UnitName.infantry, 1000)
                .withUnit(UnitName.cavalry, 1000).build();
        game.addKingdom(secondaryKingdom);
    }

    @Test
    void defenderSalvo_shouldNotKillDefenderUnits()
    {
        var attackingUnits = new UnitsMapDto();
        attackingUnits.setCount(UnitName.bowman, 100);
        attackingUnits.setCount(UnitName.infantry, 100);
        attackingUnits.setCount(UnitName.cavalry, 100);
        var data = new SendAttackDto(secondaryKingdom.getName(), AttackType.economy, attackingUnits);
        var action = new KingdomMilitaryAction(primaryKingdom, gameConfig);
        var result = action.sendAttack(data);
        assertTrue(result.success());
        assertEquals(1, primaryKingdom.getOngoingAttacks().size());

        var kingdomInteractor = new KingdomInteractor(game.getKingdomRepository());
        var ongoingAttack = primaryKingdom.getOngoingAttacks().get(0);

        var defenderBowmanCount = secondaryKingdom.getUnits().getAvailableCount(UnitName.bowman);
        var defenderInfantryCount = secondaryKingdom.getUnits().getAvailableCount(UnitName.infantry);
        var defenderCavalryCount = secondaryKingdom.getUnits().getAvailableCount(UnitName.cavalry);

        kingdomInteractor.processDefenseSalvo(primaryKingdom, secondaryKingdom, ongoingAttack);

        assertEquals(defenderBowmanCount, secondaryKingdom.getUnits().getAvailableCount(UnitName.bowman));
        assertEquals(defenderInfantryCount, secondaryKingdom.getUnits().getAvailableCount(UnitName.infantry));
        assertEquals(defenderCavalryCount, secondaryKingdom.getUnits().getAvailableCount(UnitName.cavalry));
    }

    @Test
    void defenderSalvo_shouldKillAttackerUnits()
    {
        var attackingUnits = new UnitsMapDto();
        attackingUnits.setCount(UnitName.bowman, 100);
        attackingUnits.setCount(UnitName.infantry, 100);
        attackingUnits.setCount(UnitName.cavalry, 100);
        var data = new SendAttackDto(secondaryKingdom.getName(), AttackType.economy, attackingUnits);
        var action = new KingdomMilitaryAction(primaryKingdom, gameConfig);
        var result = action.sendAttack(data);
        assertTrue(result.success());
        assertEquals(1, primaryKingdom.getOngoingAttacks().size());

        var kingdomInteractor = new KingdomInteractor(game.getKingdomRepository());
        var ongoingAttack = primaryKingdom.getOngoingAttacks().get(0);

        var attackerBowmanCountBeforeDefenseSalvo = attackingUnits.getCount(UnitName.bowman);
        var attackerInfantryCountBeforeDefenseSalvo = attackingUnits.getCount(UnitName.infantry);
        var attackerCavalryCountBeforeDefenseSalvo = attackingUnits.getCount(UnitName.cavalry);

        kingdomInteractor.processDefenseSalvo(primaryKingdom, secondaryKingdom, ongoingAttack);

        assertThat(attackerBowmanCountBeforeDefenseSalvo).isGreaterThan(attackingUnits.getCount(UnitName.bowman));
        assertThat(attackerInfantryCountBeforeDefenseSalvo).isGreaterThan(attackingUnits.getCount(UnitName.infantry));
        assertThat(attackerCavalryCountBeforeDefenseSalvo).isGreaterThan(attackingUnits.getCount(UnitName.cavalry));
    }

    @Test
    void attackerSalvo_shouldNotKillAttackerUnits()
    {
        var attackingUnits = new UnitsMapDto();
        attackingUnits.setCount(UnitName.bowman, 100);
        attackingUnits.setCount(UnitName.infantry, 100);
        attackingUnits.setCount(UnitName.cavalry, 100);
        var data = new SendAttackDto(secondaryKingdom.getName(), AttackType.economy, attackingUnits);
        var action = new KingdomMilitaryAction(primaryKingdom, gameConfig);
        var result = action.sendAttack(data);
        assertTrue(result.success());
        assertEquals(1, primaryKingdom.getOngoingAttacks().size());

        var kingdomInteractor = new KingdomInteractor(game.getKingdomRepository());
        var ongoingAttack = primaryKingdom.getOngoingAttacks().get(0);

        var attackerBowmanCount = attackingUnits.getCount(UnitName.bowman);
        var attackerInfantryCount = attackingUnits.getCount(UnitName.infantry);
        var attackerCavalryCount = attackingUnits.getCount(UnitName.cavalry);

        kingdomInteractor.processAttackSalvo(primaryKingdom, secondaryKingdom, ongoingAttack);

        assertEquals(attackerBowmanCount, attackingUnits.getCount(UnitName.bowman));
        assertEquals(attackerInfantryCount, attackingUnits.getCount(UnitName.infantry));
        assertEquals(attackerCavalryCount, attackingUnits.getCount(UnitName.cavalry));
    }

    @Test
    void attackerSalvo_shouldKillDefenderUnits()
    {
        var attackingUnits = new UnitsMapDto();
        attackingUnits.setCount(UnitName.bowman, 100);
        attackingUnits.setCount(UnitName.infantry, 100);
        attackingUnits.setCount(UnitName.cavalry, 100);
        var data = new SendAttackDto(secondaryKingdom.getName(), AttackType.economy, attackingUnits);
        var action = new KingdomMilitaryAction(primaryKingdom, gameConfig);
        var result = action.sendAttack(data);
        assertTrue(result.success());
        assertEquals(1, primaryKingdom.getOngoingAttacks().size());

        var kingdomInteractor = new KingdomInteractor(game.getKingdomRepository());
        var ongoingAttack = primaryKingdom.getOngoingAttacks().get(0);

        var defenderBowmanCountBeforeAttackerSalvo = secondaryKingdom.getUnits().getAvailableCount(UnitName.bowman);
        var defenderInfantryCountBeforeAttackerSalvo = secondaryKingdom.getUnits().getAvailableCount(UnitName.infantry);
        var defenderCavalryCountBeforeAttackerSalvo = secondaryKingdom.getUnits().getAvailableCount(UnitName.cavalry);

        kingdomInteractor.processAttackSalvo(primaryKingdom, secondaryKingdom, ongoingAttack);

        assertThat(defenderBowmanCountBeforeAttackerSalvo).isGreaterThan(secondaryKingdom.getUnits().getAvailableCount(UnitName.bowman));
        assertThat(defenderInfantryCountBeforeAttackerSalvo).isGreaterThan(secondaryKingdom.getUnits().getAvailableCount(UnitName.infantry));
        assertThat(defenderCavalryCountBeforeAttackerSalvo).isGreaterThan(secondaryKingdom.getUnits().getAvailableCount(UnitName.cavalry));
    }

    @Test
    void attackerSalvo_whenDefenderSalvoKilledAllUnits_shouldNotKillDefenderUnits()
    {
        var attackingUnits = new UnitsMapDto();
        attackingUnits.setCount(UnitName.bowman, 100);
        var data = new SendAttackDto(secondaryKingdom.getName(), AttackType.economy, attackingUnits);
        var action = new KingdomMilitaryAction(primaryKingdom, gameConfig);
        var result = action.sendAttack(data);
        assertTrue(result.success());
        assertEquals(1, primaryKingdom.getOngoingAttacks().size());

        var kingdomInteractor = new KingdomInteractor(game.getKingdomRepository());
        var ongoingAttack = primaryKingdom.getOngoingAttacks().get(0);

        var defenderBowmanCountBeforeAttackerSalvo = secondaryKingdom.getUnits().getAvailableCount(UnitName.bowman);
        var defenderInfantryCountBeforeAttackerSalvo = secondaryKingdom.getUnits().getAvailableCount(UnitName.infantry);
        var defenderCavalryCountBeforeAttackerSalvo = secondaryKingdom.getUnits().getAvailableCount(UnitName.cavalry);

        kingdomInteractor.processDefenseSalvo(primaryKingdom, secondaryKingdom, ongoingAttack);
        assertEquals(0, ongoingAttack.units.getCount(UnitName.bowman));
        kingdomInteractor.processAttackSalvo(primaryKingdom, secondaryKingdom, ongoingAttack);

        assertEquals(defenderBowmanCountBeforeAttackerSalvo, secondaryKingdom.getUnits().getAvailableCount(UnitName.bowman));
        assertEquals(defenderInfantryCountBeforeAttackerSalvo, secondaryKingdom.getUnits().getAvailableCount(UnitName.infantry));
        assertEquals(defenderCavalryCountBeforeAttackerSalvo, secondaryKingdom.getUnits().getAvailableCount(UnitName.cavalry));
    }
}
