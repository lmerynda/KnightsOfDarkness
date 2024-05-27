package com.knightsofdarkness.storage.kingdom;

import java.util.Optional;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.game.storage.IKingdomRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

@Repository
public class KingdomRepository implements IKingdomRepository {

    private final Logger log = LoggerFactory.getLogger(KingdomRepository.class);

    @Autowired
    private EntityManager entityManager;

    public Kingdom add(Kingdom kingdom)
    {
        entityManager.persist(KingdomEntity.fromDomainModel(kingdom));

        return kingdom;
    }

    public Optional<Kingdom> getKingdomByName(String name)
    {
        TypedQuery<KingdomEntity> query = entityManager.createQuery("SELECT kingdom FROM KingdomEntity kingdom WHERE kingdom.name = :name", KingdomEntity.class);
        query.setParameter("name", name);
        try
        {
            return Optional.of(query.getSingleResult().toDomainModel());
        } catch (NoResultException e)
        {
            log.warn("Kingdom with name " + name + " not found");
            return Optional.empty();
        }
    }

    public Optional<Kingdom> getKingdomById(UUID id)
    {
        log.info("Looking for kingdom with id: " + id);
        TypedQuery<KingdomEntity> query = entityManager.createQuery("SELECT kingdom FROM KingdomEntity kingdom WHERE kingdom.id = :id", KingdomEntity.class);
        query.setParameter("id", id);
        try
        {
            return Optional.of(query.getSingleResult().toDomainModel());
        } catch (NoResultException e)
        {
            log.warn("Kingdom with id " + id + " not found");
            return Optional.empty();
        }
    }
}
