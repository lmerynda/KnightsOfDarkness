package com.knightsofdarkness.web.alliance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import com.knightsofdarkness.web.Game;
import com.knightsofdarkness.web.common.alliance.CreateAllianceDto;
import com.knightsofdarkness.web.common.alliance.InviteAllianceResult;
import com.knightsofdarkness.web.common.kingdom.AttackType;
import com.knightsofdarkness.web.common.kingdom.SendAttackDto;
import com.knightsofdarkness.web.common.kingdom.UnitName;
import com.knightsofdarkness.web.common.kingdom.UnitsMapDto;
import com.knightsofdarkness.web.common.war.SpyReportDto;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;
import com.knightsofdarkness.web.kingdom.model.KingdomMilitaryAction;
import com.knightsofdarkness.web.utils.Id;
import com.knightsofdarkness.web.legacy.TestGame;
import com.knightsofdarkness.web.utils.KingdomBuilder;

public class AllianceTest
{
    private Game game;
    private KingdomEntity kingdom;
    private AllianceService allianceService;

    @BeforeEach
    void beforeEach()
    {
        game = new TestGame().get();
        kingdom = new KingdomBuilder(game).build();
        game.addKingdom(kingdom);
        allianceService = new AllianceService(game.getAllianceRepository(), game.getKingdomRepository(), game.getBotRepository(), game.getConfig(), game.getNotificationSystem(), game.getWarReportRepository());
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
    void whenEmperorHasOtherMembers_itCannotLeaveTheAlliance()
    {
        var createResult = allianceService.createAlliance(new CreateAllianceDto("Test Alliance"), kingdom.getName());
        assertTrue(createResult.success());
        var member = new KingdomBuilder(game).build();
        game.addKingdom(member);
        kingdom.getAlliance().get().addKingdom(member);

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
    void whenKingdomIsAlreadyInAlliance_itCannotBeInvited()
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
        assertTrue(createResult.success());

        InviteAllianceResult inviteResult = allianceService.inviteToAlliance(inviteeKingdom.getName(), secondEmperor.getName(), secondEmperor.getAlliance().get().getName());
        assertFalse(inviteResult.success());
        assertNotEquals(secondEmperor.getAlliance().get().getName(), inviteeKingdom.getAlliance().get().getName());
    }

    @Test
    void whenEmperorAddsBotToAlliance_newKingdomIsCreated()
    {
        var createResult = allianceService.createAlliance(new CreateAllianceDto("Test Alliance"), kingdom.getName());
        assertTrue(createResult.success());

        var botName = "Bot-" + KingdomBuilder.generateName();

        var addResult = allianceService.createNewBotAndAddToAlliance(kingdom.getName(), botName);
        assertTrue(addResult.success());

        var botKingdom = game.getKingdomRepository().getKingdomByName(botName);
        assertTrue(botKingdom.isPresent());
        assertTrue(botKingdom.get().getAlliance().isPresent());
        assertEquals(createResult.alliance().get().name(), botKingdom.get().getAlliance().get().getName());
    }

    @Test
    void whenAllianceHasMaxMembers_emperorCannotAddBotToAlliance()
    {
        var createResult = allianceService.createAlliance(new CreateAllianceDto("Test Alliance"), kingdom.getName());
        assertTrue(createResult.success());

        for (int i = 0; i < game.getConfig().common().allianceMaxMembers() - 1; i++)
        {
            var botName = "Bot-" + KingdomBuilder.generateName();
            var addResult = allianceService.createNewBotAndAddToAlliance(kingdom.getName(), botName);
            assertTrue(addResult.success());
        }

        var botName = "Bot-" + KingdomBuilder.generateName();
        var addResult = allianceService.createNewBotAndAddToAlliance(kingdom.getName(), botName);
        assertFalse(addResult.success());
    }

    @Test
    void whenEmperorIsAlone_leavingShouldDisbandTheAlliance()
    {
        allianceService.createAlliance(new CreateAllianceDto("Lonely Crown"), kingdom.getName());

        assertTrue(allianceService.leaveAlliance(kingdom.getName()).success());

        assertTrue(kingdom.getAlliance().isEmpty());
        assertTrue(game.getAllianceRepository().getAllianceByName("Lonely Crown").isEmpty());
    }

    @Test
    void creatingSecondAllianceOrDuplicateName_shouldFail()
    {
        assertTrue(allianceService.createAlliance(new CreateAllianceDto("Iron Pact"), kingdom.getName()).success());
        assertFalse(allianceService.createAlliance(new CreateAllianceDto("Second Pact"), kingdom.getName()).success());

        var other = new KingdomBuilder(game).build();
        game.addKingdom(other);
        assertFalse(allianceService.createAlliance(new CreateAllianceDto("iron pact"), other.getName()).success());
        assertFalse(allianceService.createAlliance(new CreateAllianceDto("  "), other.getName()).success());
    }

    @Test
    void invitedKingdom_shouldSeeAndAcceptItsInvitation()
    {
        allianceService.createAlliance(new CreateAllianceDto("Iron Pact"), kingdom.getName());
        var invitee = new KingdomBuilder(game).build();
        game.addKingdom(invitee);

        assertTrue(allianceService.inviteToAlliance(invitee.getName(), kingdom.getName(), "Iron Pact").success());
        assertFalse(allianceService.inviteToAlliance(invitee.getName(), kingdom.getName(), "Iron Pact").success());

        var invitations = allianceService.getInvitations(invitee.getName());
        assertEquals(1, invitations.size());
        assertEquals("Iron Pact", invitations.get(0).allianceName());
        assertTrue(game.getNotificationSystem().getNotifications(invitee.getName()).stream().anyMatch(notification -> notification.message().contains("Iron Pact")));

        assertTrue(allianceService.acceptAllianceInvitation(invitee.getName(), invitations.get(0).id()).success());
        assertEquals("Iron Pact", invitee.getAlliance().get().getName());
        assertTrue(allianceService.getInvitations(invitee.getName()).isEmpty());
    }

    @Test
    void rejectedInvitation_shouldDisappear()
    {
        allianceService.createAlliance(new CreateAllianceDto("Iron Pact"), kingdom.getName());
        var invitee = new KingdomBuilder(game).build();
        game.addKingdom(invitee);
        var invitation = allianceService.inviteToAlliance(invitee.getName(), kingdom.getName(), "Iron Pact").invitation().get();

        assertTrue(allianceService.rejectAllianceInvitation(invitee.getName(), invitation.id()).success());

        assertTrue(allianceService.getInvitations(invitee.getName()).isEmpty());
        assertTrue(invitee.getAlliance().isEmpty());
    }

    @Test
    void invitingKingdomAlreadyInAlliance_shouldFail()
    {
        allianceService.createAlliance(new CreateAllianceDto("Iron Pact"), kingdom.getName());
        var other = new KingdomBuilder(game).build();
        game.addKingdom(other);
        allianceService.createAlliance(new CreateAllianceDto("Other Pact"), other.getName());

        assertFalse(allianceService.inviteToAlliance(other.getName(), kingdom.getName(), "Iron Pact").success());
    }

    private SpyReportDto intel(String spy, String target)
    {
        return new SpyReportDto(Id.generate(), Instant.now(), spy, target, true, 5, 0, Map.of(), Map.of(), Map.of(UnitName.infantry, 7));
    }

    @Test
    void warRoom_shouldShareTheMembersArmiesAndIntel()
    {
        allianceService.createAlliance(new CreateAllianceDto("Iron Pact"), kingdom.getName());
        var member = new KingdomBuilder(game).withName("member").withUnit(UnitName.cavalry, 20).build();
        game.addKingdom(member);
        kingdom.getAlliance().get().addKingdom(member);
        var enemy = new KingdomBuilder(game).withName("enemy").build();
        game.addKingdom(enemy);
        var stranger = new KingdomBuilder(game).withName("stranger").build();
        game.addKingdom(stranger);

        var army = new UnitsMapDto();
        army.setCount(UnitName.cavalry, 20);
        assertTrue(new KingdomMilitaryAction(member, game.getConfig()).sendAttack(new SendAttackDto("enemy", AttackType.land, army), enemy).success());
        game.getWarReportRepository().addSpyReport(intel("member", "enemy"));
        game.getWarReportRepository().addSpyReport(intel("stranger", "member"));

        var warRoom = allianceService.getWarRoom(kingdom.getName()).orElseThrow();

        assertThat(warRoom.marching()).singleElement().satisfies(march -> {
            assertEquals("member", march.from);
            assertEquals("enemy", march.to);
        });
        assertThat(warRoom.intel()).extracting(SpyReportDto::targetKingdomName).containsExactly("enemy");
        assertThat(warRoom.members()).extracting(strength -> strength.kingdomName()).containsExactlyInAnyOrder(kingdom.getName(), "member");
    }

    @Test
    void warRoom_shouldBeEmptyWithoutAnAlliance()
    {
        assertTrue(allianceService.getWarRoom(kingdom.getName()).isEmpty());
    }
}
