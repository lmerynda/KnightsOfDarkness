package com.knightsofdarkness.storage.kingdom;

import java.util.Optional;

import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public class KingdomRepository {
    private final KingdomJpaRepository jpaRepository;

    public KingdomRepository(KingdomJpaRepository jpaRepository)
    {
        this.jpaRepository = jpaRepository;
    }

    public void add(KingdomEntity kingdom)
    {
        kingdom.resources.syncResources();
        kingdom.buildings.syncBuildings();
        jpaRepository.save(kingdom);
    }

    public Optional<KingdomEntity> getKingdomByName(String name)
    {
        return jpaRepository.findById(name);
    }

    public List<KingdomEntity> getAllKingdoms()
    {
        return jpaRepository.findAll();
    }

    public void update(KingdomEntity kingdom)
    {
        kingdom.resources.syncResources();
        kingdom.buildings.syncBuildings();
        jpaRepository.save(kingdom);
    }
}
