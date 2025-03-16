package com.knightsofdarkness.web.alliance;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.knightsofdarkness.common.alliance.AcceptAllianceInvitationResult;
import com.knightsofdarkness.common.alliance.AllianceDto;
import com.knightsofdarkness.common.alliance.CreateAllianceDto;
import com.knightsofdarkness.common.alliance.CreateAllianceResult;
import com.knightsofdarkness.common.alliance.InviteAllianceResult;
import com.knightsofdarkness.common.alliance.LeaveAllianceResult;
import com.knightsofdarkness.common.alliance.RejectAllianceInvitationResult;
import com.knightsofdarkness.web.alliance.model.AllianceEntity;
import com.knightsofdarkness.web.alliance.model.AllianceInvitationEntity;
import com.knightsofdarkness.web.kingdom.IKingdomRepository;
import com.knightsofdarkness.web.utils.Id;

import jakarta.transaction.Transactional;

@Service
public class AllianceService {
    private final IAllianceRepository allianceRepository;
    private final IKingdomRepository kingdomRepository;

    public AllianceService(IAllianceRepository allianceRepository, IKingdomRepository kingdomRepository)
    {
        this.allianceRepository = allianceRepository;
        this.kingdomRepository = kingdomRepository;
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
    public boolean removeFromAlliance(String kingdomName, String emperor)
    {
        var maybeKingdom = kingdomRepository.getKingdomByName(kingdomName);
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

        if (!alliance.get().getEmperor().equals(emperor))
        {
            return false;
        }

        kingdom.removeAlliance();
        kingdomRepository.update(kingdom);
        return true;
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

    public List<AllianceDto> getAlliances()
    {
        var alliances = allianceRepository.getAlliances();
        return alliances.stream().map(alliance -> new AllianceDto(alliance.getName(), alliance.getEmperor())).toList();
    }
}
