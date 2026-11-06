package com.knightsofdarkness.web.alliance.model;

import java.util.Optional;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.knightsofdarkness.web.alliance.IAllianceRepository;

import jakarta.persistence.EntityManager;

@Repository
public class AllianceRepository implements IAllianceRepository {
    private final AllianceJpaRepository allianceJpaRepository;
    private final AllianceInvitationJpaRepository allianceInvitationRepository;
    private final EntityManager entityManager;

    public AllianceRepository(AllianceJpaRepository allianceJpaRepository, AllianceInvitationJpaRepository allianceInvitationRepository, EntityManager entityManager)
    {
        this.entityManager = entityManager;
        this.allianceJpaRepository = allianceJpaRepository;
        this.allianceInvitationRepository = allianceInvitationRepository;
    }

    @Override
    public Optional<AllianceEntity> getAllianceByName(String name)
    {
        return allianceJpaRepository.findById(name);
    }

    @Override
    public Optional<AllianceEntity> lockAllianceByName(String name)
    {
        return allianceJpaRepository.findByNameForUpdate(name);
    }

    @Override
    /**
     * inserts the alliance; its name is the key, so a second alliance of one name founded at the same moment breaks the
     * key and is refused, where a save would quietly overwrite the first
     */
    public void create(AllianceEntity alliance)
    {
        entityManager.persist(alliance);
        entityManager.flush();
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

    @Override
    public List<AllianceInvitationEntity> getInvitationsByKingdomName(String kingdomName)
    {
        return allianceInvitationRepository.findByKingdomName(kingdomName);
    }

    @Override
    public void delete(AllianceEntity alliance)
    {
        allianceInvitationRepository.deleteByAllianceName(alliance.getName());
        allianceJpaRepository.delete(alliance);
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
