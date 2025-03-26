package com.knightsofdarkness.web.alliance.model;

import java.util.Optional;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.knightsofdarkness.web.alliance.IAllianceRepository;

@Repository
public class AllianceRepository implements IAllianceRepository {
    private final AllianceJpaRepository allianceJpaRepository;
    private final AllianceInvitationJpaRepository allianceInvitationRepository;

    public AllianceRepository(AllianceJpaRepository allianceJpaRepository, AllianceInvitationJpaRepository allianceInvitationRepository)
    {
        this.allianceJpaRepository = allianceJpaRepository;
        this.allianceInvitationRepository = allianceInvitationRepository;
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

    public AllianceInvitationEntity createInvitation(AllianceInvitationEntity invitation)
    {
        return allianceInvitationRepository.save(invitation);
    }

    @Override
    public Optional<AllianceInvitationEntity> getInvitationById(UUID id)
    {
        return allianceInvitationRepository.findById(id);
    }

    @Override
    public void deleteInvitation(AllianceInvitationEntity invitation)
    {
        allianceInvitationRepository.delete(invitation);
    }
}
