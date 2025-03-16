package com.knightsofdarkness.web.alliance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.knightsofdarkness.common.alliance.CreateAllianceDto;
import com.knightsofdarkness.common.alliance.InviteAllianceResult;
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

    @Test
    void whenKingdomIsNotInAlliance_itCannnotLeaveTheAlliance()
    {
        var createResult = allianceService.createAlliance(new CreateAllianceDto("Test Alliance"), kingdom.getName());
        assertTrue(createResult.success());

        var otherKingdom = new KingdomBuilder(game).build();
        game.addKingdom(otherKingdom);
        assertTrue(otherKingdom.getAlliance().isEmpty());

        var leaveResult = allianceService.leaveAlliance(otherKingdom.getName());
        assertFalse(leaveResult.success());
    }

    @Test
    void whenKingdomIsAllianceEmperor_itCanRemoveAnotherKingdomFromAlliance()
    {
        var createResult = allianceService.createAlliance(new CreateAllianceDto("Test Alliance"), kingdom.getName());
        assertTrue(createResult.success());
        var alliance = kingdom.getAlliance().get();

        var otherKingdom = new KingdomBuilder(game).build();
        game.addKingdom(otherKingdom);
        alliance.addKingdom(otherKingdom);

        var removeResult = allianceService.removeFromAlliance(otherKingdom.getName(), kingdom.getName());
        assertTrue(removeResult.success());
        assertFalse(otherKingdom.getAlliance().isPresent());
    }

    @Test
    void whenKingdomIsNotAllianceEmperor_itCannotRemoveAnotherKingdomFromAlliance()
    {
        var createResult = allianceService.createAlliance(new CreateAllianceDto("Test Alliance"), kingdom.getName());
        assertTrue(createResult.success());
        var alliance = kingdom.getAlliance().get();

        var otherKingdom = new KingdomBuilder(game).build();
        game.addKingdom(otherKingdom);
        alliance.addKingdom(otherKingdom);

        var nonEmperorKingdom = new KingdomBuilder(game).build();
        game.addKingdom(nonEmperorKingdom);

        var removeResult = allianceService.removeFromAlliance(otherKingdom.getName(), nonEmperorKingdom.getName());
        assertFalse(removeResult.success());
    }

    @Test
    void whenKingdomIsNotInAlliance_itCannotRemoveAnotherKingdomFromAlliance()
    {
        var createResult = allianceService.createAlliance(new CreateAllianceDto("Test Alliance"), kingdom.getName());
        assertTrue(createResult.success());

        var otherKingdom = new KingdomBuilder(game).build();
        game.addKingdom(otherKingdom);
        assertTrue(otherKingdom.getAlliance().isEmpty());

        var removeResult = allianceService.removeFromAlliance(otherKingdom.getName(), kingdom.getName());
        assertFalse(removeResult.success());
    }

    @Test
    void whenKingdomIsAllianceEmperor_itCanInviteAnotherKingdomToAlliance()
    {
        var createResult = allianceService.createAlliance(new CreateAllianceDto("Test Alliance"), kingdom.getName());
        assertTrue(createResult.success());
        var alliance = kingdom.getAlliance().get();

        var inviteeKingdom = new KingdomBuilder(game).build();
        game.addKingdom(inviteeKingdom);

        var inviteResult = allianceService.inviteToAlliance(inviteeKingdom.getName(), kingdom.getName(), alliance.getName());
        assertTrue(inviteResult.success());
    }

    @Test
    void whenKingdomIsNotAllianceEmperor_itCannotInviteAnotherKingdomToAlliance()
    {
        var createResult = allianceService.createAlliance(new CreateAllianceDto("Test Alliance"), kingdom.getName());
        assertTrue(createResult.success());
        var alliance = kingdom.getAlliance().get();

        var nonEmperorKingdom = new KingdomBuilder(game).build();
        game.addKingdom(nonEmperorKingdom);

        var inviteeKingdom = new KingdomBuilder(game).build();
        game.addKingdom(inviteeKingdom);

        var inviteResult = allianceService.inviteToAlliance(inviteeKingdom.getName(), nonEmperorKingdom.getName(), alliance.getName());
        assertFalse(inviteResult.success());
    }

    @Test
    void whenAllianceDoesNotExist_kingdomCannotInviteAnotherKingdomToAlliance()
    {
        var inviteeKingdom = new KingdomBuilder(game).build();
        game.addKingdom(inviteeKingdom);

        var inviteResult = allianceService.inviteToAlliance(inviteeKingdom.getName(), kingdom.getName(), "NonExistentAlliance");
        assertFalse(inviteResult.success());
    }

    @Test

    void whenKingdomAcceptsInvitation_itJoinsTheAlliance()
    {
        var createResult = allianceService.createAlliance(new CreateAllianceDto("Test Alliance"), kingdom.getName());
        assertTrue(createResult.success());
        var alliance = kingdom.getAlliance().get();

        var inviteeKingdom = new KingdomBuilder(game).build();
        game.addKingdom(inviteeKingdom);

        InviteAllianceResult inviteResult = allianceService.inviteToAlliance(inviteeKingdom.getName(), kingdom.getName(), alliance.getName());
        assertTrue(inviteResult.success());

        var acceptResult = allianceService.acceptAllianceInvitation(inviteeKingdom.getName(), inviteResult.invitation().get().id());
        assertTrue(acceptResult.success());
        assertTrue(inviteeKingdom.getAlliance().isPresent());
        assertEquals(alliance.getName(), inviteeKingdom.getAlliance().get().getName());
    }

    @Test
    void whenKingdomIsAlreadyInAlliance_itCannotAcceptInvitation()
    {
        var createResult = allianceService.createAlliance(new CreateAllianceDto("Test Alliance"), kingdom.getName());
        assertTrue(createResult.success());
        var alliance = kingdom.getAlliance().get();

        var inviteeKingdom = new KingdomBuilder(game).build();
        game.addKingdom(inviteeKingdom);
        alliance.addKingdom(inviteeKingdom);

        var secondEmperor = new KingdomBuilder(game).build();
        game.addKingdom(secondEmperor);
        createResult = allianceService.createAlliance(new CreateAllianceDto("Test Alliance 2"), secondEmperor.getName());

        InviteAllianceResult inviteResult = allianceService.inviteToAlliance(inviteeKingdom.getName(), secondEmperor.getName(), secondEmperor.getAlliance().get().getName());
        assertTrue(inviteResult.success());

        var acceptResult = allianceService.acceptAllianceInvitation(inviteeKingdom.getName(), inviteResult.invitation().get().id());
        assertFalse(acceptResult.success());
        assertNotEquals(secondEmperor.getAlliance().get().getName(), inviteeKingdom.getAlliance().get().getName());
    }
}
