package com.knightsofdarkness.web.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.web.Game;
import com.knightsofdarkness.web.common.kingdom.AttackType;
import com.knightsofdarkness.web.common.kingdom.BuildingName;
import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.kingdom.SendAttackDto;
import com.knightsofdarkness.web.common.kingdom.UnitName;
import com.knightsofdarkness.web.common.kingdom.UnitsMapDto;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.kingdom.IKingdomInteractor;
import com.knightsofdarkness.web.kingdom.model.KingdomDetailsProvider;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;
import com.knightsofdarkness.web.kingdom.model.KingdomMilitaryAction;
import com.knightsofdarkness.web.kingdom.model.KingdomTurnAction;
import com.knightsofdarkness.web.legacy.TestGame;
import com.knightsofdarkness.web.utils.KingdomBuilder;

class KingdomAttackTest {
    private Game game;
    private IKingdomInteractor kingdomInteractor;
    private KingdomEntity primaryKingdom;
    private KingdomEntity secondaryKingdom;
    private GameConfig gameConfig;
    private KingdomDetailsProvider kingdomDetailsProvider;
    private static final int weaponsProductionPercentage = 0;

    @BeforeEach
    void setUp()
    {
        game = new TestGame().get();
        gameConfig = game.getConfig();
        kingdomInteractor = game.getKingdomInteractor();
        kingdomDetailsProvider = new KingdomDetailsProvider(gameConfig);
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
        var action = new KingdomMilitaryAction(primaryKingdom, gameConfig);
        var result = action.sendAttack(data);
        assertTrue(result.success());
        assertEquals(1, primaryKingdom.getOngoingAttacks().size());

        var numberOfTurns = gameConfig.attack().turnsToArrive();
        for (int i = 0; i < numberOfTurns; i++)
        {
            var turnAction = new KingdomTurnAction(primaryKingdom, kingdomInteractor, gameConfig, kingdomDetailsProvider);
            turnAction.passTurn(weaponsProductionPercentage);
        }
        assertEquals(0, primaryKingdom.getOngoingAttacks().size());
    }

    @Test
    void whenPrimaryKingdomSendsSoldiers_unitsShouldMoveToMobile()
    {
        var turnAction = new KingdomTurnAction(primaryKingdom, kingdomInteractor, gameConfig, kingdomDetailsProvider);
        turnAction.passTurn(weaponsProductionPercentage);// normalize kingdom population
        var initialPopulation = primaryKingdom.getUnits().countAll();
        var attackingUnits = new UnitsMapDto();
        attackingUnits.setCount(UnitName.bowman, 100);
        attackingUnits.setCount(UnitName.infantry, 100);
        attackingUnits.setCount(UnitName.cavalry, 100);
        var data = new SendAttackDto(secondaryKingdom.getName(), AttackType.economy, attackingUnits);
        var action = new KingdomMilitaryAction(primaryKingdom, gameConfig);
        var result = action.sendAttack(data);
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
    void whenPrimaryKingdomSendsSoldiersAndCompletesAttack_thenAttackerWillLoseUnitsOnDefendentSalvo()
    {
        var turnAction = new KingdomTurnAction(primaryKingdom, kingdomInteractor, gameConfig, kingdomDetailsProvider);
        turnAction.passTurn(weaponsProductionPercentage);// normalize kingdom population
        var attackingUnits = new UnitsMapDto();
        attackingUnits.setCount(UnitName.bowman, 100);
        attackingUnits.setCount(UnitName.infantry, 100);
        attackingUnits.setCount(UnitName.cavalry, 100);
        var data = new SendAttackDto(secondaryKingdom.getName(), AttackType.economy, attackingUnits);
        var action = new KingdomMilitaryAction(primaryKingdom, gameConfig);
        var result = action.sendAttack(data);
        assertTrue(result.success());
        var initialPopulation = primaryKingdom.getUnits().countAll();
        var numberOfTurns = gameConfig.attack().turnsToArrive();
        for (int i = 0; i < numberOfTurns; i++)
        {
            turnAction.passTurn(weaponsProductionPercentage);
        }
        assertThat(initialPopulation).isGreaterThan(primaryKingdom.getUnits().countAll());
        assertEquals(0, primaryKingdom.getOngoingAttacks().size());

        assertEquals(900, primaryKingdom.getUnits().getAvailableCount(UnitName.bowman));
        assertEquals(900, primaryKingdom.getUnits().getAvailableCount(UnitName.infantry));
        assertEquals(900, primaryKingdom.getUnits().getAvailableCount(UnitName.cavalry));
    }

    @Test
    void whenPrimaryKingdomSendsSoldiersAndCompletesAttack_thenDefendentWillLoseUnitsOnAttackerSalvo()
    {
        var turnAction = new KingdomTurnAction(primaryKingdom, kingdomInteractor, gameConfig, kingdomDetailsProvider);
        turnAction.passTurn(weaponsProductionPercentage);// normalize kingdom population
        var attackingUnits = new UnitsMapDto();
        attackingUnits.setCount(UnitName.bowman, 1000);
        attackingUnits.setCount(UnitName.infantry, 1000);
        attackingUnits.setCount(UnitName.cavalry, 1000);
        var data = new SendAttackDto(secondaryKingdom.getName(), AttackType.economy, attackingUnits);
        var action = new KingdomMilitaryAction(primaryKingdom, gameConfig);
        var result = action.sendAttack(data);
        assertTrue(result.success());
        var initialPopulation = secondaryKingdom.getUnits().countAll();
        var numberOfTurns = gameConfig.attack().turnsToArrive();
        for (int i = 0; i < numberOfTurns; i++)
        {
            turnAction.passTurn(weaponsProductionPercentage);
        }
        assertThat(initialPopulation).isGreaterThan(secondaryKingdom.getUnits().countAll());
        assertEquals(0, primaryKingdom.getOngoingAttacks().size());

        assertEquals(665, secondaryKingdom.getUnits().getAvailableCount(UnitName.bowman));
        assertEquals(777, secondaryKingdom.getUnits().getAvailableCount(UnitName.infantry));
        assertEquals(832, secondaryKingdom.getUnits().getAvailableCount(UnitName.cavalry));
    }
}
