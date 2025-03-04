package com.knightsofdarkness.web.alliance;

import java.util.List;

import org.springframework.stereotype.Service;

import com.knightsofdarkness.common.alliance.AllianceDto;
import com.knightsofdarkness.common.alliance.CreateAllianceDto;
import com.knightsofdarkness.web.alliance.model.AllianceRepository;

import jakarta.transaction.Transactional;

@Service
public class AllianceService {
    private final AllianceRepository allianceRepository;

    public AllianceService(AllianceRepository allianceRepository)
    {
        this.allianceRepository = allianceRepository;
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
