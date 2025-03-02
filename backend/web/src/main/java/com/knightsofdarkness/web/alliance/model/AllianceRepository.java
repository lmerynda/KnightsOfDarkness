package com.knightsofdarkness.web.alliance.model;

import java.util.Optional;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.knightsofdarkness.common.alliance.CreateAllianceDto;
import com.knightsofdarkness.web.alliance.IAllianceRepository;
import com.knightsofdarkness.web.game.config.GameConfig;

@Repository
public class AllianceRepository implements IAllianceRepository {
    private final GameConfig gameConfig;
    private final AllianceJpaRepository allianceJpaRepository;

    public AllianceRepository(GameConfig gameConfig, AllianceJpaRepository allianceJpaRepository)
    {
        this.gameConfig = gameConfig;
        this.allianceJpaRepository = allianceJpaRepository;
    }

    @Override
    public Optional<AllianceEntity> getAllianceByName(String name)
    {
        return allianceJpaRepository.findById(name);
    }

    @Override
    public void add(CreateAllianceDto allianceDto, String emperor)
    {
        var alliance = AllianceEntity.fromDto(allianceDto, emperor);
        allianceJpaRepository.save(alliance);
    }

    @Override
    public void update(AllianceEntity alliance)
    {
        allianceJpaRepository.save(alliance);
    }

    @Override
    public List<AllianceEntity> getAlliances()
    {
        return allianceJpaRepository.findAll();
    }
}
