package com.knightsofdarkness.game.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.common.kingdom.AttackType;
import com.knightsofdarkness.common.kingdom.BuildingName;
import com.knightsofdarkness.common.kingdom.ResourceName;
import com.knightsofdarkness.common.kingdom.SendAttackDto;
import com.knightsofdarkness.common.kingdom.UnitName;
import com.knightsofdarkness.common.kingdom.UnitsMapDto;
import com.knightsofdarkness.game.Game;
import com.knightsofdarkness.game.TestGame;
import com.knightsofdarkness.game.interactions.IKingdomInteractor;
import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.game.utils.KingdomBuilder;

class KingdomAttackTest {
    private Game game;
    private IKingdomInteractor kingdomInteractor;
    private Kingdom primaryKingdom;
    private Kingdom secondaryKingdom;

    @BeforeEach
    void setUp()
    {
        game = new TestGame().get();
        kingdomInteractor = game.getKingdomInteractor();
        primaryKingdom = new KingdomBuilder(game).withName("primary").withResource(ResourceName.land, 2500).withBuilding(BuildingName.house, 1000).withBuilding(BuildingName.barracks, 1000).withUnit(UnitName.bowman, 1000)
                .withUnit(UnitName.infantry, 1000)
                .withUnit(UnitName.cavalry, 1000).build();
        game.addKingdom(primaryKingdom);
        secondaryKingdom = new KingdomBuilder(game).withName("secondary").withResource(ResourceName.land, 2500).withBuilding(BuildingName.house, 1000).withBuilding(BuildingName.barracks, 1000).withUnit(UnitName.bowman, 1000)
                .withUnit(UnitName.infantry, 1000)
                .withUnit(UnitName.cavalry, 1000).build();
        game.addKingdom(secondaryKingdom);
    }

    @Test
    void whenPrimaryKingdomSendsSoldiersAndCompletesAttack_thenAttackResolves()
    {
        var attackingUnits = new UnitsMapDto();
        attackingUnits.setCount(UnitName.bowman, 100);
        attackingUnits.setCount(UnitName.infantry, 100);
        attackingUnits.setCount(UnitName.cavalry, 100);
        var data = new SendAttackDto(secondaryKingdom.getName(), AttackType.economy, attackingUnits);
        var result = primaryKingdom.sendAttack(data);
        assertTrue(result.success());
        assertEquals(1, primaryKingdom.getOngoingAttacks().size());

        var numberOfTurns = 4;
        for (int i = 0; i < numberOfTurns; i++)
        {
            primaryKingdom.passTurn(kingdomInteractor);
        }
        assertEquals(0, primaryKingdom.getOngoingAttacks().size());
    }

    @Test
    void whenPrimaryKingdomSendsSoldiers_unitsShouldMoveToMobile()
    {
        primaryKingdom.passTurn(kingdomInteractor); // normalize kingdom population
        var initialPopulation = primaryKingdom.getUnits().countAll();
        var attackingUnits = new UnitsMapDto();
        attackingUnits.setCount(UnitName.bowman, 100);
        attackingUnits.setCount(UnitName.infantry, 100);
        attackingUnits.setCount(UnitName.cavalry, 100);
        var data = new SendAttackDto(secondaryKingdom.getName(), AttackType.economy, attackingUnits);
        var result = primaryKingdom.sendAttack(data);
        assertTrue(result.success());
        assertEquals(initialPopulation, primaryKingdom.getUnits().countAll());
        assertEquals(900, primaryKingdom.getUnits().getAvailableCount(UnitName.bowman));
        assertEquals(900, primaryKingdom.getUnits().getAvailableCount(UnitName.infantry));
        assertEquals(900, primaryKingdom.getUnits().getAvailableCount(UnitName.cavalry));

        assertEquals(100, primaryKingdom.getUnits().getMobileCount(UnitName.bowman));
        assertEquals(100, primaryKingdom.getUnits().getMobileCount(UnitName.infantry));
        assertEquals(100, primaryKingdom.getUnits().getMobileCount(UnitName.cavalry));
    }

    @Test
    void whenPrimaryKingdomSendsSoldiersAndCompletesAttack_thenAttackerWillLoseUnits()
    {
        primaryKingdom.passTurn(kingdomInteractor); // normalize kingdom population
        var attackingUnits = new UnitsMapDto();
        attackingUnits.setCount(UnitName.bowman, 100);
        attackingUnits.setCount(UnitName.infantry, 100);
        attackingUnits.setCount(UnitName.cavalry, 100);
        var data = new SendAttackDto(secondaryKingdom.getName(), AttackType.economy, attackingUnits);
        var result = primaryKingdom.sendAttack(data);
        assertTrue(result.success());
        var initialPopulation = primaryKingdom.getUnits().countAll();
        var numberOfTurns = 3;
        for (int i = 0; i < numberOfTurns; i++)
        {
            primaryKingdom.passTurn(kingdomInteractor);
        }
        primaryKingdom.passTurn(kingdomInteractor); // trigger attack resolution
        assertThat(initialPopulation).isGreaterThan(primaryKingdom.getUnits().countAll());
        assertEquals(0, primaryKingdom.getOngoingAttacks().size());

        assertEquals(900, primaryKingdom.getUnits().getAvailableCount(UnitName.bowman));
        assertEquals(900, primaryKingdom.getUnits().getAvailableCount(UnitName.infantry));
        assertEquals(917, primaryKingdom.getUnits().getAvailableCount(UnitName.cavalry));
    }
}
