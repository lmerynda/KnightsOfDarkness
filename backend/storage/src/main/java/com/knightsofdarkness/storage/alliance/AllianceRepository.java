package com.knightsofdarkness.storage.alliance;

import java.util.Optional;

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
    public void add(CreateAllianceDto alliance, String emperor)
    {
        allianceJpaRepository.save(AllianceEntity.fromDto(alliance, emperor));
    }

    @Override
    public void update(Alliance alliance)
    {
        allianceJpaRepository.save(AllianceEntity.fromDomainModel(alliance));
    }
}
