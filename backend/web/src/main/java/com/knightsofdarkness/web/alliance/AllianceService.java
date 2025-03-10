package com.knightsofdarkness.web.alliance;

import java.util.List;

import org.springframework.stereotype.Service;

import com.knightsofdarkness.common.alliance.AllianceDto;
import com.knightsofdarkness.common.alliance.CreateAllianceDto;
import com.knightsofdarkness.common.alliance.CreateAllianceResult;
import com.knightsofdarkness.common.alliance.LeaveAllianceResult;
import com.knightsofdarkness.web.alliance.model.AllianceEntity;
import com.knightsofdarkness.web.kingdom.IKingdomRepository;

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

    public List<AllianceDto> getAlliances()
    {
        var alliances = allianceRepository.getAlliances();
        return alliances.stream().map(alliance -> new AllianceDto(alliance.getName(), alliance.getEmperor())).toList();
    }
}
