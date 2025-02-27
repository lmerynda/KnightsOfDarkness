package com.knightsofdarkness.web.alliance.model;

import java.util.Optional;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.knightsofdarkness.common.alliance.CreateAllianceDto;
import com.knightsofdarkness.game.alliance.Alliance;
import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.game.storage.IAllianceRepository;

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
    public Optional<Alliance> getAllianceByName(String name)
    {
        return allianceJpaRepository.findById(name).map(alliance -> alliance.toDomainModel(gameConfig));
    }

    @Override
    public void add(CreateAllianceDto allianceDto, String emperor)
    {
        var alliance = AllianceEntity.fromDto(allianceDto, emperor);
        allianceJpaRepository.save(alliance);
    }

    @Override
    public void update(Alliance alliance)
    {
        allianceJpaRepository.save(AllianceEntity.fromDomainModel(alliance));
    }

    @Override
    public List<Alliance> getAlliances()
    {
        return allianceJpaRepository.findAll().stream().map(alliance -> alliance.toDomainModel(gameConfig)).collect(Collectors.toList());
    }
}
