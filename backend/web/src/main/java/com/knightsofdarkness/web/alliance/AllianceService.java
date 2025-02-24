package com.knightsofdarkness.web.alliance;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.knightsofdarkness.common.alliance.AllianceDto;
import com.knightsofdarkness.common.alliance.CreateAllianceDto;
import com.knightsofdarkness.common.alliance.CreateAllianceResult;
import com.knightsofdarkness.game.Utils;
import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.storage.alliance.AllianceRepository;
import com.knightsofdarkness.storage.kingdom.KingdomRepository;

import jakarta.transaction.Transactional;

@Service
public class AllianceService {
    private final Logger log = LoggerFactory.getLogger(AllianceService.class);
    private final AllianceRepository allianceRepository;
    private final KingdomRepository kingdomRepository;
    private final GameConfig gameConfig;

    public AllianceService(AllianceRepository allianceRepository, KingdomRepository kingdomRepository, GameConfig gameConfig)
    {
        this.allianceRepository = allianceRepository;
        this.kingdomRepository = kingdomRepository;
        this.gameConfig = gameConfig;
    }

    @Transactional
    public CreateAllianceResult createAlliance(CreateAllianceDto createAllianceDto, String emperor)
    {
        var existingAlliance = allianceRepository.getAllianceByName(createAllianceDto.name());
        if (existingAlliance.isPresent())
        {
            log.warn("Alliance with name {} already exists", createAllianceDto.name());
            return CreateAllianceResult.failure("Alliance with name " + createAllianceDto.name() + " already exists");
        }

        var maybeKingdom = kingdomRepository.getKingdomByName(emperor);
        if (maybeKingdom.isEmpty())
        {
            log.warn("Kingdom with name {} not found", emperor);
            return CreateAllianceResult.failure(Utils.format("Kingdom with name {} not found", emperor));
        }
        Kingdom kingdom = maybeKingdom.get();

        allianceRepository.add(createAllianceDto, emperor);
        kingdomRepository.update(kingdom);
        log.info("Alliance {} created successfully by emperor {}", createAllianceDto.name(), emperor);
        return CreateAllianceResult.success("Alliance created successfully");
    }

    public List<AllianceDto> getAlliances()
    {
        var alliances = allianceRepository.getAlliances();
        return alliances.stream().map(alliance -> new AllianceDto(alliance.getName(), alliance.getEmperor())).toList();
    }
}
