package com.knightsofdarkness.web.alliance;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.knightsofdarkness.web.alliance.model.AllianceEntity;
import com.knightsofdarkness.web.alliance.model.AllianceInvitationEntity;
import com.knightsofdarkness.web.bots.IBotRepository;
import com.knightsofdarkness.web.bots.model.BotEntity;
import com.knightsofdarkness.web.common.alliance.AcceptAllianceInvitationResult;
import com.knightsofdarkness.web.common.alliance.AllianceDto;
import com.knightsofdarkness.web.common.alliance.CreateAllianceDto;
import com.knightsofdarkness.web.common.alliance.CreateAllianceResult;
import com.knightsofdarkness.web.common.alliance.InviteAllianceResult;
import com.knightsofdarkness.web.common.alliance.LeaveAllianceResult;
import com.knightsofdarkness.web.common.alliance.RejectAllianceInvitationResult;
import com.knightsofdarkness.web.common.alliance.RemoveFromAllianceResult;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.kingdom.IKingdomRepository;
import com.knightsofdarkness.web.kingdom.model.KingdomCreator;
import com.knightsofdarkness.web.utils.Id;

import jakarta.transaction.Transactional;

@Service
public class AllianceService {
    private final IAllianceRepository allianceRepository;
    private final IKingdomRepository kingdomRepository;
    private final IBotRepository botRepository;
    private final GameConfig gameConfig;

    public AllianceService(IAllianceRepository allianceRepository, IKingdomRepository kingdomRepository, IBotRepository botRepository, GameConfig gameConfig)
    {
        this.allianceRepository = allianceRepository;
        this.kingdomRepository = kingdomRepository;
        this.botRepository = botRepository;
        this.gameConfig = gameConfig;
    }

    @Transactional
    public CreateAllianceResult createAlliance(CreateAllianceDto createAllianceDto, String emperor)
    {
        var kingdom = kingdomRepository.getKingdomByName(emperor);
        if (kingdom.isEmpty())
        {
            return CreateAllianceResult.failure("Kingdom not found");
        }
        var alliance = new AllianceEntity(createAllianceDto.name(), emperor);
        alliance.addKingdom(kingdom.get());
        allianceRepository.create(alliance);
        return CreateAllianceResult.success("Alliance Created", alliance.toDto());
    }

    @Transactional
    public LeaveAllianceResult leaveAlliance(String kingdomName)
    {
        var maybeKingdom = kingdomRepository.getKingdomByName(kingdomName);
        if (maybeKingdom.isEmpty())
        {
            return LeaveAllianceResult.failure("Kingdom not found");
        }
        var kingdom = maybeKingdom.get();

        var alliance = kingdom.getAlliance();
        if (alliance.isEmpty())
        {
            return LeaveAllianceResult.failure("Kingdom is not in an alliance");
        }

        if (alliance.get().getEmperor().equals(kingdomName))
        {
            // TODO fix to check if emperor is the last kingdom in the alliance
            return LeaveAllianceResult.failure("Emperor cannot leave the alliance");
        }

        kingdom.removeAlliance();
        kingdomRepository.update(kingdom);
        return LeaveAllianceResult.success("You've left the alliance");
    }

    @Transactional
    public RemoveFromAllianceResult removeFromAlliance(String kingdomName, String emperor)
    {
        var maybeKingdom = kingdomRepository.getKingdomByName(kingdomName);
        if (maybeKingdom.isEmpty())
        {
            return RemoveFromAllianceResult.failure("Kingdom not found");
        }
        var kingdom = maybeKingdom.get();

        var alliance = kingdom.getAlliance();
        if (alliance.isEmpty())
        {
            return RemoveFromAllianceResult.failure("Kingdom is not in an alliance");
        }

        if (!alliance.get().getEmperor().equals(emperor))
        {
            return RemoveFromAllianceResult.failure("Only the emperor can remove kingdoms from the alliance");
        }

        kingdom.removeAlliance();
        kingdomRepository.update(kingdom);
        return RemoveFromAllianceResult.success("Kingdom has been removed from the alliance");
    }

    @Transactional
    public InviteAllianceResult inviteToAlliance(String invitee, String emperor, String allianceName)
    {
        var maybeKingdom = kingdomRepository.getKingdomByName(invitee);
        if (maybeKingdom.isEmpty())
        {
            return InviteAllianceResult.failure("Invitee not found");
        }
        var kingdom = maybeKingdom.get();

        // TODO it should be in repository, we do not need to fetch the alliance to check its property
        var maybeAlliance = allianceRepository.getAllianceByName(allianceName);
        if (maybeAlliance.isEmpty())
        {
            return InviteAllianceResult.failure("Alliance not found");
        }
        var alliance = maybeAlliance.get();

        if (!alliance.getEmperor().equals(emperor))
        {
            return InviteAllianceResult.failure("Only the emperor can invite other kingdoms to the alliance");
        }

        var invitation = new AllianceInvitationEntity(Id.generate(), Instant.now(), kingdom, alliance);
        invitation = allianceRepository.createInvitation(invitation);

        return InviteAllianceResult.success("Invitation sent to " + invitee, invitation.toDto());
    }

    @Transactional
    public AcceptAllianceInvitationResult acceptAllianceInvitation(String kingdomName, UUID invitationId)
    {
        var maybeKingdom = kingdomRepository.getKingdomByName(kingdomName);
        if (maybeKingdom.isEmpty())
        {
            return AcceptAllianceInvitationResult.failure("Kingdom not found");
        }
        var kingdom = maybeKingdom.get();

        if (kingdom.getAlliance().isPresent())
        {
            return AcceptAllianceInvitationResult.failure("Kingdom is already in an alliance");
        }

        var maybeInvitation = allianceRepository.getInvitationById(invitationId);
        if (maybeInvitation.isEmpty())
        {
            return AcceptAllianceInvitationResult.failure("Invitation not found");
        }
        var invitation = maybeInvitation.get();

        if (!invitation.getKingdom().getName().equals(kingdomName))
        {
            return AcceptAllianceInvitationResult.failure("Invitation does not belong to this kingdom");
        }

        var alliance = invitation.getAlliance();
        alliance.addKingdom(kingdom);
        kingdomRepository.update(kingdom);
        allianceRepository.update(alliance);
        allianceRepository.deleteInvitation(invitation);
        // TODO notification

        return AcceptAllianceInvitationResult.success("You've joined the alliance");
    }

    @Transactional
    public RejectAllianceInvitationResult rejectAllianceInvitation(String kingdomName, UUID invitationId)
    {
        var maybeInvitation = allianceRepository.getInvitationById(invitationId);
        if (maybeInvitation.isEmpty())
        {
            return RejectAllianceInvitationResult.failure("Invitation not found");
        }
        var invitation = maybeInvitation.get();

        if (!invitation.getKingdom().getName().equals(kingdomName))
        {
            return RejectAllianceInvitationResult.failure("Invitation does not belong to this kingdom");
        }

        allianceRepository.deleteInvitation(invitation);
        // TODO notification

        return RejectAllianceInvitationResult.success("You've rejected the alliance invitation");
    }

    @Transactional
    public boolean createNewBotAndAddToAlliance(String emperor, String botName)
    {
        var maybeKingdom = kingdomRepository.getKingdomByName(emperor);
        if (maybeKingdom.isEmpty())
        {
            return false;
        }
        var kingdom = maybeKingdom.get();

        var alliance = kingdom.getAlliance();
        if (alliance.isEmpty())
        {
            return false;
        }

        var existingKingdom = kingdomRepository.getKingdomByName(botName);
        if (existingKingdom.isPresent())
        {
            // new bot must have unique name
            return false;
        }

        var kingdomCreator = new KingdomCreator(kingdomRepository, gameConfig);
        var botKingdom = kingdomCreator.createKingdom(botName);

        alliance.get().addKingdom(botKingdom);
        botRepository.add(new BotEntity(Id.generate(), botKingdom));
        kingdomRepository.update(botKingdom);
        return true;
    }

    public List<AllianceDto> getAlliances()
    {
        var alliances = allianceRepository.getAlliances();
        return alliances.stream().map(alliance -> new AllianceDto(alliance.getName(), alliance.getEmperor())).toList();
    }
}
