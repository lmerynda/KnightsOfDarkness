package com.knightsofdarkness.web.alliance;

import java.util.List;

import org.springframework.stereotype.Service;

import com.knightsofdarkness.common.alliance.AllianceDto;
import com.knightsofdarkness.common.alliance.CreateAllianceDto;
import com.knightsofdarkness.common.alliance.CreateAllianceResult;
import com.knightsofdarkness.web.alliance.model.AllianceEntity;
import com.knightsofdarkness.web.alliance.model.AllianceRepository;
import com.knightsofdarkness.web.kingdom.model.KingdomRepository;

import jakarta.transaction.Transactional;

@Service
public class AllianceService {
    private final AllianceRepository allianceRepository;
    private final KingdomRepository kingdomRepository;

    public AllianceService(AllianceRepository allianceRepository, KingdomRepository kingdomRepository)
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

    public List<AllianceDto> getAlliances()
    {
        var alliances = allianceRepository.getAlliances();
        return alliances.stream().map(alliance -> new AllianceDto(alliance.getName(), alliance.getEmperor())).toList();
    }
}
