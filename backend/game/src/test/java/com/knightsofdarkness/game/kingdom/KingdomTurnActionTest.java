package com.knightsofdarkness.game.kingdom;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.game.Game;
import com.knightsofdarkness.game.TestGame;
import com.knightsofdarkness.game.utils.KingdomBuilder;

class KingdomTurnActionTest {
    private static Game game;
    private KingdomBuilder kingdomBuilder;
    private Kingdom kingdom;

    @BeforeAll
    static void beforeAll()
    {
        game = new TestGame().get();
    }

    @BeforeEach
    void setUp()
    {
        this.kingdomBuilder = new KingdomBuilder(game);
        this.kingdom = kingdomBuilder.build();
    }

    @Test
    void testGetBonusFactorBasedOnLand()
    {
        KingdomTurnAction kingdomTurnAction = new KingdomTurnAction(kingdom, null);

        // Test for minimum land value
        double bonus1 = kingdomTurnAction.getBonusFactorBasedOnLand(1000);
        assertEquals(1.0, bonus1);

        // Test for maximum land value
        double bonus2 = kingdomTurnAction.getBonusFactorBasedOnLand(100);
        assertEquals(5.000, bonus2, 0.01);

        // Test for intermediate land value
        double bonus3 = kingdomTurnAction.getBonusFactorBasedOnLand(500);
        assertEquals(1.559, bonus3, 0.01);

        // Test for land value below the minimum
        double bonus4 = kingdomTurnAction.getBonusFactorBasedOnLand(2000);
        assertEquals(1.0, bonus4);

        // Test for land value above the maximum
        double bonus5 = kingdomTurnAction.getBonusFactorBasedOnLand(50);
        assertEquals(6.07, bonus5, 0.01);
    }

    @Test
    void testGetKingdomSizeProductionBonus()
    {
        KingdomTurnAction kingdomTurnAction = new KingdomTurnAction(kingdom, null);

        double bonusBelow100 = kingdomTurnAction.getKingdomSizeProductionBonus(99);
        double bonusAt100 = kingdomTurnAction.getKingdomSizeProductionBonus(100);
        assertEquals(bonusAt100, bonusBelow100);

        double bonus2 = kingdomTurnAction.getKingdomSizeProductionBonus(1000);
        assertEquals(1.0, bonus2);

        double bonusOver1000 = kingdomTurnAction.getKingdomSizeProductionBonus(1001);
        double bonusAt1000 = kingdomTurnAction.getKingdomSizeProductionBonus(1000);
        assertEquals(bonusAt1000, bonusOver1000);

        assertEquals(5.0, bonusAt100, 0.01);

        double bonusAt200 = kingdomTurnAction.getKingdomSizeProductionBonus(200);
        assertEquals(3.47, bonusAt200, 0.01);

        double bonusAt300 = kingdomTurnAction.getKingdomSizeProductionBonus(300);
        assertEquals(2.52, bonusAt300, 0.01);

        double bonusAt400 = kingdomTurnAction.getKingdomSizeProductionBonus(400);
        assertEquals(1.93, bonusAt400, 0.01);

        double bonusAt500 = kingdomTurnAction.getKingdomSizeProductionBonus(500);
        assertEquals(1.55, bonusAt500, 0.01);

        double bonusAt600 = kingdomTurnAction.getKingdomSizeProductionBonus(600);
        assertEquals(1.32, bonusAt600, 0.01);

        double bonusAt700 = kingdomTurnAction.getKingdomSizeProductionBonus(700);
        assertEquals(1.18, bonusAt700, 0.01);

        double bonusAt800 = kingdomTurnAction.getKingdomSizeProductionBonus(800);
        assertEquals(1.09, bonusAt800, 0.01);

        double bonusAt900 = kingdomTurnAction.getKingdomSizeProductionBonus(900);
        assertEquals(1.03, bonusAt900, 0.01);
    }
}
