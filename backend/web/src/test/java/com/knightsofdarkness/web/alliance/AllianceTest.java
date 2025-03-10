package com.knightsofdarkness.web.alliance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.common.alliance.CreateAllianceDto;
import com.knightsofdarkness.web.Game;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;
import com.knightsofdarkness.web.legacy.TestGame;
import com.knightsofdarkness.web.utils.KingdomBuilder;

public class AllianceTest {
    private Game game;
    private KingdomEntity kingdom;
    private AllianceService allianceService;

    @BeforeEach
    void beforeEach()
    {
        game = new TestGame().get();
        kingdom = new KingdomBuilder(game).build();
        game.addKingdom(kingdom);
        allianceService = new AllianceService(game.getAllianceRepository(), game.getKingdomRepository());
    }

    @Test
    void testCreateAlliance()
    {
        var result = allianceService.createAlliance(new CreateAllianceDto("Test Alliance"), kingdom.getName());

        assertTrue(result.success());
        assertTrue(result.alliance().isPresent());
        assertEquals("Test Alliance", result.alliance().get().name());
        assertEquals(kingdom.getName(), result.alliance().get().emperor());
    }

    @Test
    void whenKingdomIsAllianceEmperor_itCannotLeaveTheAlliance()
    {
        var createResult = allianceService.createAlliance(new CreateAllianceDto("Test Alliance"), kingdom.getName());
        assertTrue(createResult.success());

        var leaveResult = allianceService.leaveAlliance(kingdom.getName());
        assertFalse(leaveResult.success());
        assertTrue(kingdom.getAlliance().isPresent());
    }

    @Disabled("fix emperor cannot leave the alliance error, when leaving kingdom is not the emperor")
    @Test
    void whenKingdomIsNotAllianceEmperor_itCanLeaveTheAlliance()
    {
        var createResult = allianceService.createAlliance(new CreateAllianceDto("Test Alliance"), kingdom.getName());
        assertTrue(createResult.success());
        var alliance = kingdom.getAlliance().get();

        var otherKingdom = new KingdomBuilder(game).build();
        game.addKingdom(otherKingdom);
        alliance.addKingdom(otherKingdom);

        var leaveResult = allianceService.leaveAlliance(otherKingdom.getName());
        assertTrue(leaveResult.success());
        assertTrue(otherKingdom.getAlliance().isEmpty());
    }
}
