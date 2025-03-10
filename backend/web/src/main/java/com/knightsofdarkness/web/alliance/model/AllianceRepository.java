package com.knightsofdarkness.web.alliance.model;

import java.util.Optional;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.knightsofdarkness.web.alliance.IAllianceRepository;

@Repository
public class AllianceRepository implements IAllianceRepository {
    private final AllianceJpaRepository allianceJpaRepository;

    public AllianceRepository(AllianceJpaRepository allianceJpaRepository)
    {
        this.allianceJpaRepository = allianceJpaRepository;
    }

    @Override
    public Optional<AllianceEntity> getAllianceByName(String name)
    {
        return allianceJpaRepository.findById(name);
    }

    @Override
    public void create(AllianceEntity alliance)
    {
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
