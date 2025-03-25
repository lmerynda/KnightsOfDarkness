package com.knightsofdarkness.web.kingdom.model;

import java.util.Optional;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.knightsofdarkness.web.kingdom.IKingdomRepository;

@Repository
public class KingdomRepository implements IKingdomRepository {
    private final KingdomJpaRepository jpaRepository;

    public KingdomRepository(KingdomJpaRepository jpaRepository)
    {
        this.jpaRepository = jpaRepository;
    }

    public KingdomEntity add(KingdomEntity kingdom)
    {
        kingdom.resources.syncResources();
        kingdom.buildings.syncBuildings();
        kingdom.units.syncUnits();
        return jpaRepository.save(kingdom);
    }

    public Optional<KingdomEntity> getKingdomByName(String name)
    {
        return jpaRepository.findById(name);
    }

    public List<KingdomEntity> getAllKingdoms()
    {
        return jpaRepository.findAll();
    }

    public KingdomEntity update(KingdomEntity kingdom)
    {
        kingdom.resources.syncResources();
        kingdom.buildings.syncBuildings();
        kingdom.units.syncUnits();
        return jpaRepository.save(kingdom);
    }
}
