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

class KingdomInteractorAttackTest
{
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
        var result = action.sendAttack(data, secondaryKingdom);
        assertTrue(result.success());
        assertEquals(1, primaryKingdom.getOngoingAttacks().size());

        var kingdomInteractor = (KingdomInteractor) game.getKingdomInteractor();
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
        var result = action.sendAttack(data, secondaryKingdom);
        assertTrue(result.success());
        assertEquals(1, primaryKingdom.getOngoingAttacks().size());

        var kingdomInteractor = (KingdomInteractor) game.getKingdomInteractor();
        var ongoingAttack = primaryKingdom.getOngoingAttacks().get(0);

        var attackerBowmanCountBeforeDefenseSalvo = attackingUnits.getCount(UnitName.bowman);
        var attackerInfantryCountBeforeDefenseSalvo = attackingUnits.getCount(UnitName.infantry);
        var attackerCavalryCountBeforeDefenseSalvo = attackingUnits.getCount(UnitName.cavalry);

        kingdomInteractor.processDefenseSalvo(primaryKingdom, secondaryKingdom, ongoingAttack);

        assertThat(attackerBowmanCountBeforeDefenseSalvo).isGreaterThan(ongoingAttack.units.getCount(UnitName.bowman));
        assertThat(attackerInfantryCountBeforeDefenseSalvo).isGreaterThan(ongoingAttack.units.getCount(UnitName.infantry));
        assertThat(attackerCavalryCountBeforeDefenseSalvo).isGreaterThan(ongoingAttack.units.getCount(UnitName.cavalry));
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
        var result = action.sendAttack(data, secondaryKingdom);
        assertTrue(result.success());
        assertEquals(1, primaryKingdom.getOngoingAttacks().size());

        var kingdomInteractor = (KingdomInteractor) game.getKingdomInteractor();
        var ongoingAttack = primaryKingdom.getOngoingAttacks().get(0);

        var attackerBowmanCount = attackingUnits.getCount(UnitName.bowman);
        var attackerInfantryCount = attackingUnits.getCount(UnitName.infantry);
        var attackerCavalryCount = attackingUnits.getCount(UnitName.cavalry);

        kingdomInteractor.processAttackSalvo(secondaryKingdom, ongoingAttack);

        assertEquals(attackerBowmanCount, ongoingAttack.units.getCount(UnitName.bowman));
        assertEquals(attackerInfantryCount, ongoingAttack.units.getCount(UnitName.infantry));
        assertEquals(attackerCavalryCount, ongoingAttack.units.getCount(UnitName.cavalry));
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
        var result = action.sendAttack(data, secondaryKingdom);
        assertTrue(result.success());
        assertEquals(1, primaryKingdom.getOngoingAttacks().size());

        var kingdomInteractor = (KingdomInteractor) game.getKingdomInteractor();
        var ongoingAttack = primaryKingdom.getOngoingAttacks().get(0);

        var defenderBowmanCountBeforeAttackerSalvo = secondaryKingdom.getUnits().getAvailableCount(UnitName.bowman);
        var defenderInfantryCountBeforeAttackerSalvo = secondaryKingdom.getUnits().getAvailableCount(UnitName.infantry);
        var defenderCavalryCountBeforeAttackerSalvo = secondaryKingdom.getUnits().getAvailableCount(UnitName.cavalry);

        kingdomInteractor.processAttackSalvo(secondaryKingdom, ongoingAttack);

        assertThat(defenderBowmanCountBeforeAttackerSalvo).isGreaterThan(secondaryKingdom.getUnits().getAvailableCount(UnitName.bowman));
        assertThat(defenderInfantryCountBeforeAttackerSalvo).isGreaterThan(secondaryKingdom.getUnits().getAvailableCount(UnitName.infantry));
        assertThat(defenderCavalryCountBeforeAttackerSalvo).isGreaterThan(secondaryKingdom.getUnits().getAvailableCount(UnitName.cavalry));
    }

    @Test
    void attackerSalvo_shouldKillDefenderUnitsOnce()
    {
        var attackingUnits = new UnitsMapDto();
        attackingUnits.setCount(UnitName.bowman, 100);
        var data = new SendAttackDto(secondaryKingdom.getName(), AttackType.economy, attackingUnits);
        var action = new KingdomMilitaryAction(primaryKingdom, gameConfig);
        assertTrue(action.sendAttack(data, secondaryKingdom).success());

        var kingdomInteractor = (KingdomInteractor) game.getKingdomInteractor();
        kingdomInteractor.processAttackSalvo(secondaryKingdom, primaryKingdom.getOngoingAttacks().get(0));

        // 100 bowmen split evenly over 3 defending unit types hit 34 each: 34/3, 34/5, 34/7
        assertEquals(1000 - 11, secondaryKingdom.getUnits().getAvailableCount(UnitName.bowman));
        assertEquals(1000 - 6, secondaryKingdom.getUnits().getAvailableCount(UnitName.infantry));
        assertEquals(1000 - 4, secondaryKingdom.getUnits().getAvailableCount(UnitName.cavalry));
    }

    private KingdomOngoingAttackEntity attackWith(KingdomEntity attacker, KingdomEntity defender, UnitsMapDto units)
    {
        var action = new KingdomMilitaryAction(attacker, gameConfig);
        assertTrue(action.sendAttack(new SendAttackDto(defender.getName(), AttackType.economy, units), defender).success());
        return attacker.getOngoingAttacks().get(0);
    }

    private KingdomEntity kingdomWith(String name, UnitName unit, int count)
    {
        var kingdom = new KingdomBuilder(game).withName(name).withUnit(unit, count).build();
        game.addKingdom(kingdom);
        return kingdom;
    }

    @Test
    void melee_shouldLetInfantryAndCavalryStrikeEveryEnemyType()
    {
        var riders = kingdomWith("riders", UnitName.cavalry, 150);
        var footmen = kingdomWith("footmen", UnitName.infantry, 300);
        var charge = new UnitsMapDto();
        charge.setCount(UnitName.cavalry, 150);
        var attack = attackWith(riders, footmen, charge);

        ((KingdomInteractor) game.getKingdomInteractor()).processMelee(riders, footmen, attack);

        // 300 infantry blows on cavalry that takes 4 each, 150 cavalry blows on infantry that takes 3 each
        assertEquals(150 - 75, attack.units.getCount(UnitName.cavalry));
        assertEquals(300 - 50, footmen.getUnits().getAvailableCount(UnitName.infantry));
    }

    @Test
    void melee_shouldLetBowmenFallWithoutStriking()
    {
        var archers = kingdomWith("archers", UnitName.bowman, 100);
        var footmen = kingdomWith("footmen", UnitName.infantry, 100);
        var volley = new UnitsMapDto();
        volley.setCount(UnitName.bowman, 100);
        var attack = attackWith(archers, footmen, volley);

        ((KingdomInteractor) game.getKingdomInteractor()).processMelee(archers, footmen, attack);

        // bowmen land no melee blows, 100 infantry blows on bowmen that take 2 each
        assertEquals(100, footmen.getUnits().getAvailableCount(UnitName.infantry));
        assertEquals(100 - 50, attack.units.getCount(UnitName.bowman));
    }

    @Test
    void melee_shouldSpreadBlowsOverBowmenToo()
    {
        var footmen = kingdomWith("footmen", UnitName.infantry, 120);
        var charge = new UnitsMapDto();
        charge.setCount(UnitName.infantry, 120);
        var attack = attackWith(footmen, secondaryKingdom, charge);

        ((KingdomInteractor) game.getKingdomInteractor()).processMelee(footmen, secondaryKingdom, attack);

        // 120 blows split over the defender's three equal unit types, 40 each: 40/2, 40/3, 40/4
        assertEquals(1000 - 20, secondaryKingdom.getUnits().getAvailableCount(UnitName.bowman));
        assertEquals(1000 - 13, secondaryKingdom.getUnits().getAvailableCount(UnitName.infantry));
        assertEquals(1000 - 10, secondaryKingdom.getUnits().getAvailableCount(UnitName.cavalry));
    }

    @Test
    void attackerSalvo_whenDefenderSalvoKilledAllUnits_shouldNotKillDefenderUnits()
    {
        var attackingUnits = new UnitsMapDto();
        attackingUnits.setCount(UnitName.bowman, 100);
        var data = new SendAttackDto(secondaryKingdom.getName(), AttackType.economy, attackingUnits);
        var action = new KingdomMilitaryAction(primaryKingdom, gameConfig);
        var result = action.sendAttack(data, secondaryKingdom);
        assertTrue(result.success());
        assertEquals(1, primaryKingdom.getOngoingAttacks().size());

        var kingdomInteractor = (KingdomInteractor) game.getKingdomInteractor();
        var ongoingAttack = primaryKingdom.getOngoingAttacks().get(0);

        var defenderBowmanCountBeforeAttackerSalvo = secondaryKingdom.getUnits().getAvailableCount(UnitName.bowman);
        var defenderInfantryCountBeforeAttackerSalvo = secondaryKingdom.getUnits().getAvailableCount(UnitName.infantry);
        var defenderCavalryCountBeforeAttackerSalvo = secondaryKingdom.getUnits().getAvailableCount(UnitName.cavalry);

        kingdomInteractor.processDefenseSalvo(primaryKingdom, secondaryKingdom, ongoingAttack);
        assertEquals(0, ongoingAttack.units.getCount(UnitName.bowman));
        kingdomInteractor.processAttackSalvo(secondaryKingdom, ongoingAttack);

        assertEquals(defenderBowmanCountBeforeAttackerSalvo, secondaryKingdom.getUnits().getAvailableCount(UnitName.bowman));
        assertEquals(defenderInfantryCountBeforeAttackerSalvo, secondaryKingdom.getUnits().getAvailableCount(UnitName.infantry));
        assertEquals(defenderCavalryCountBeforeAttackerSalvo, secondaryKingdom.getUnits().getAvailableCount(UnitName.cavalry));
    }
}
