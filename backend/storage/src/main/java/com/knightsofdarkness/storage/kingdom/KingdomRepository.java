package com.knightsofdarkness.storage.kingdom;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.game.storage.IKingdomRepository;

@Repository
public class KingdomRepository implements IKingdomRepository {
    private final GameConfig gameConfig;
    private final KingdomJpaRepository jpaRepository;

    public KingdomRepository(GameConfig gameConfig, KingdomJpaRepository jpaRepository)
    {
        this.gameConfig = gameConfig;
        this.jpaRepository = jpaRepository;
    }

    public Kingdom add(Kingdom kingdom)
    {
        var kingdomEntity = jpaRepository.save(KingdomEntity.fromDomainModel(kingdom));
        return kingdomEntity.toDomainModel(gameConfig);
    }

    public Optional<Kingdom> getKingdomByName(String name)
    {
        var kingdom = jpaRepository.findById(name);
        return kingdom.map(kingdomEntity -> kingdomEntity.toDomainModel(gameConfig));
    }

    public Kingdom update(Kingdom kingdom)
    {
        var kingdomEntity = jpaRepository.save(KingdomEntity.fromDomainModel(kingdom));
        return kingdomEntity.toDomainModel(gameConfig);
    }
}
