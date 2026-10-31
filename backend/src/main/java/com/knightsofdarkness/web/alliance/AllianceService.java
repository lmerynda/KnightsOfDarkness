package com.knightsofdarkness.web.alliance;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.knightsofdarkness.web.alliance.model.AllianceEntity;
import com.knightsofdarkness.web.alliance.model.AllianceInvitationEntity;
import com.knightsofdarkness.web.bots.IBotRepository;
import com.knightsofdarkness.web.bots.model.BotEntity;
import com.knightsofdarkness.web.common.alliance.AcceptAllianceInvitationResult;
import com.knightsofdarkness.web.common.alliance.AddBotToAllianceResult;
import com.knightsofdarkness.web.common.alliance.AllianceDto;
import com.knightsofdarkness.web.common.alliance.AllianceInvitationDto;
import com.knightsofdarkness.web.common.alliance.AllianceMemberStrengthDto;
import com.knightsofdarkness.web.common.alliance.AllianceWarRoomDto;
import com.knightsofdarkness.web.common.alliance.AllianceWithMembersDto;
import com.knightsofdarkness.web.common.alliance.CreateAllianceDto;
import com.knightsofdarkness.web.common.alliance.CreateAllianceResult;
import com.knightsofdarkness.web.common.alliance.InviteAllianceResult;
import com.knightsofdarkness.web.common.alliance.LeaveAllianceResult;
import com.knightsofdarkness.web.common.alliance.RejectAllianceInvitationResult;
import com.knightsofdarkness.web.common.alliance.RemoveFromAllianceResult;
import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.messaging.NotificationKind;
import com.knightsofdarkness.web.common.war.BattleReportDto;
import com.knightsofdarkness.web.common.war.SpyReportDto;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.kingdom.IKingdomRepository;
import com.knightsofdarkness.web.kingdom.model.KingdomCreator;
import com.knightsofdarkness.web.kingdom.model.KingdomDetailsProvider;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;
import com.knightsofdarkness.web.kingdom.model.KingdomOngoingAttackEntity;
import com.knightsofdarkness.web.kingdom.model.KingdomReinforcementEntity;
import com.knightsofdarkness.web.messaging.INotificationSystem;
import com.knightsofdarkness.web.utils.Id;
import com.knightsofdarkness.web.war.IWarReportRepository;

import jakarta.transaction.Transactional;

@Service
public class AllianceService {
    private final IAllianceRepository allianceRepository;
    private final IKingdomRepository kingdomRepository;
    private final IBotRepository botRepository;
    private final GameConfig gameConfig;
    private final INotificationSystem notificationSystem;
    private final IWarReportRepository warReportRepository;
    private static final int warRoomReportsPerMember = 10;

    public AllianceService(IAllianceRepository allianceRepository, IKingdomRepository kingdomRepository, IBotRepository botRepository, GameConfig gameConfig, INotificationSystem notificationSystem,
            IWarReportRepository warReportRepository)
    {
        this.warReportRepository = warReportRepository;
        this.notificationSystem = notificationSystem;
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
        if (kingdom.get().getAlliance().isPresent())
        {
            return CreateAllianceResult.failure("Leave your current alliance first");
        }
        var name = createAllianceDto.name() == null ? "" : createAllianceDto.name().strip();
        if (name.length() < 3 || name.length() > 32)
        {
            return CreateAllianceResult.failure("Alliance name must be 3 to 32 characters long");
        }
        if (allianceRepository.getAlliances().stream().anyMatch(existing -> existing.getName().equalsIgnoreCase(name)))
        {
            return CreateAllianceResult.failure("An alliance with this name already exists");
        }
        var alliance = new AllianceEntity(name, emperor);
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
            if (alliance.get().getMembersCount() > 1)
            {
                return LeaveAllianceResult.failure("The emperor can leave only after every other member is gone");
            }

            kingdom.removeAlliance();
            kingdomRepository.update(kingdom);
            allianceRepository.delete(alliance.get());
            return LeaveAllianceResult.success("You've left and the alliance was disbanded");
        }

        var allianceEntity = alliance.get();
        kingdom.removeAlliance();
        kingdomRepository.update(kingdom);
        notificationSystem.create(allianceEntity.getEmperor(), NotificationKind.alliance, kingdomName + " has left " + allianceEntity.getName());
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

        var allianceName = alliance.get().getName();
        kingdom.removeAlliance();
        kingdomRepository.update(kingdom);
        notificationSystem.create(kingdomName, NotificationKind.alliance, "You were removed from " + allianceName);
        return RemoveFromAllianceResult.success(String.format("Kingdom %s has been removed from the alliance", kingdomName));
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
        if (kingdom.getAlliance().isPresent())
        {
            return InviteAllianceResult.failure(invitee + " is already in an alliance");
        }
        if (alliance.getMembersCount() >= gameConfig.common().allianceMaxMembers())
        {
            return InviteAllianceResult.failure("The alliance is full");
        }
        if (allianceRepository.getInvitationsByKingdomName(invitee).stream().anyMatch(existing -> existing.getAlliance().getName().equals(allianceName)))
        {
            return InviteAllianceResult.failure(invitee + " is already invited");
        }

        var invitation = new AllianceInvitationEntity(Id.generate(), Instant.now(), kingdom, alliance);
        invitation = allianceRepository.createInvitation(invitation);
        notificationSystem.create(invitee, NotificationKind.alliance, emperor + " invites you to join " + allianceName);

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
        if (alliance.getMembersCount() >= gameConfig.common().allianceMaxMembers())
        {
            return AcceptAllianceInvitationResult.failure("The alliance is full");
        }
        alliance.addKingdom(kingdom);
        kingdomRepository.update(kingdom);
        allianceRepository.update(alliance);
        allianceRepository.getInvitationsByKingdomName(kingdomName).forEach(allianceRepository::deleteInvitation);
        notificationSystem.create(alliance.getEmperor(), NotificationKind.alliance, kingdomName + " has joined " + alliance.getName());

        return AcceptAllianceInvitationResult.success("You've joined the alliance");
    }

    public List<AllianceInvitationDto> getInvitations(String kingdomName)
    {
        return allianceRepository.getInvitationsByKingdomName(kingdomName).stream().map(AllianceInvitationEntity::toDto).toList();
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
        notificationSystem.create(invitation.getAlliance().getEmperor(), NotificationKind.alliance, kingdomName + " declined the invitation to " + invitation.getAlliance().getName());

        return RejectAllianceInvitationResult.success("You've rejected the alliance invitation");
    }

    @Transactional
    public AddBotToAllianceResult createNewBotAndAddToAlliance(String emperor, String botName)
    {
        var maybeKingdom = kingdomRepository.getKingdomByName(emperor);
        if (maybeKingdom.isEmpty())
        {
            return AddBotToAllianceResult.failure("Emperor kingdom not found");
        }
        var kingdom = maybeKingdom.get();

        var alliance = kingdom.getAlliance();
        if (alliance.isEmpty())
        {
            return AddBotToAllianceResult.failure("Requested emperor is not part of any alliance");
        }

        var membersCount = alliance.get().getMembersCount();
        if (membersCount >= gameConfig.common().allianceMaxMembers())
        {
            return AddBotToAllianceResult.failure("Alliance has reached maximum members limit");
        }

        var existingKingdom = kingdomRepository.getKingdomByName(botName);
        if (existingKingdom.isPresent())
        {
            return AddBotToAllianceResult.failure("New kingdom must have a unique name");
        }

        var kingdomCreator = new KingdomCreator(gameConfig);
        var kingdomToCreate = kingdomCreator.createKingdom(botName);
        var botKingdom = kingdomRepository.add(kingdomToCreate);
        alliance.get().addKingdom(botKingdom);
        botRepository.add(new BotEntity(Id.generate(), botKingdom));

        return AddBotToAllianceResult.success("Bot kingdom created and added to alliance");
    }

    public List<AllianceDto> getAlliances()
    {
        var alliances = allianceRepository.getAlliances();
        return alliances.stream().map(alliance -> new AllianceDto(alliance.getName(), alliance.getEmperor())).toList();
    }

    public Optional<AllianceWithMembersDto> getKingdomAlliance(String kingdomName)
    {
        var maybeKingdom = kingdomRepository.getKingdomByName(kingdomName);
        if (maybeKingdom.isEmpty())
        {
            return Optional.empty();
        }
        var kingdom = maybeKingdom.get();

        var alliance = kingdom.getAlliance();
        if (alliance.isEmpty())
        {
            return Optional.empty();
        }

        return Optional.of(alliance.get().toWithMembersDto());
    }

    /**
     * the members' armies on the march, soonest first; the freshest successful intel on each kingdom outside the alliance
     * from any member; and the members' recent battles
     */
    @Transactional
    public Optional<AllianceWarRoomDto> getWarRoom(String kingdomName)
    {
        var alliance = kingdomRepository.getKingdomByName(kingdomName).flatMap(KingdomEntity::getAlliance);
        if (alliance.isEmpty())
        {
            return Optional.empty();
        }

        var members = alliance.get().getKingdoms();
        var names = members.stream().map(KingdomEntity::getName).collect(Collectors.toSet());
        var marching = members.stream().flatMap(member -> member.getOngoingAttacks().stream()).map(KingdomOngoingAttackEntity::toDto)
                .sorted(Comparator.comparingInt(march -> march.turnsLeft)).toList();

        Map<String, SpyReportDto> freshest = new HashMap<>();
        Map<UUID, BattleReportDto> battles = new HashMap<>();
        for (var member : names)
        {
            warReportRepository.getSpyReports(member, warRoomReportsPerMember).stream()
                    .filter(report -> report.success() && names.contains(report.kingdomName()) && !names.contains(report.targetKingdomName()))
                    .forEach(report -> freshest.merge(report.targetKingdomName(), report, (one, other) -> one.date().isAfter(other.date()) ? one : other));
            warReportRepository.getBattleReports(member, warRoomReportsPerMember).forEach(report -> battles.put(report.id(), report));
        }
        var intel = freshest.values().stream().sorted(Comparator.comparing(SpyReportDto::date).reversed()).toList();
        var recentBattles = battles.values().stream().sorted(Comparator.comparing(BattleReportDto::date).reversed()).limit(warRoomReportsPerMember).toList();
        var guards = members.stream().flatMap(member -> member.getReinforcements().stream()).map(KingdomReinforcementEntity::toDto).toList();
        var strength = new KingdomDetailsProvider(gameConfig);
        var standing = members.stream().map(member -> new AllianceMemberStrengthDto(member.getName(), member.getResources().getCount(ResourceName.land), strength.getDefensePower(member)))
                .sorted(Comparator.comparingDouble(member -> member.defense() / Math.max(1, member.land()))).toList();
        return Optional.of(new AllianceWarRoomDto(standing, marching, guards, intel, recentBattles));
    }
}
