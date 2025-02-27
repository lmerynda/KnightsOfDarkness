package com.knightsofdarkness.web.alliance;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.knightsofdarkness.common.alliance.AllianceDto;
import com.knightsofdarkness.common.alliance.CreateAllianceDto;
import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.web.alliance.model.AllianceRepository;

import jakarta.transaction.Transactional;

@Service
public class AllianceService {
    private final Logger log = LoggerFactory.getLogger(AllianceService.class);
    private final AllianceRepository allianceRepository;
    private final GameConfig gameConfig;

    public AllianceService(AllianceRepository allianceRepository, GameConfig gameConfig)
    {
        this.allianceRepository = allianceRepository;
        this.gameConfig = gameConfig;
    }

    @Transactional
    public void createAlliance(CreateAllianceDto createAllianceDto, String emperor)
    {
        allianceRepository.add(createAllianceDto, emperor);
    }

    public List<AllianceDto> getAlliances()
    {
        var alliances = allianceRepository.getAlliances();
        return alliances.stream().map(alliance -> new AllianceDto(alliance.getName(), alliance.getEmperor())).toList();
    }
}
