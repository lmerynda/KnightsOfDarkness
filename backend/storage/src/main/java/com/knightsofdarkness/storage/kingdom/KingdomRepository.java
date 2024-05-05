package com.knightsofdarkness.storage.kingdom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.game.storage.IKingdomRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

@Repository
public class KingdomRepository implements IKingdomRepository {

    @Autowired
    private EntityManager entityManager;

    public Kingdom add(Kingdom kingdom)
    {
        entityManager.persist(KingdomEntity.fromDomainModel(kingdom));

        return kingdom;
    }

    public Kingdom getKingdomByName(String name)
    {
        TypedQuery<KingdomEntity> query = entityManager.createQuery("SELECT kingdom FROM KingdomEntity kingdom WHERE kingdom.name = :name", KingdomEntity.class);
        query.setParameter("name", name);
        query.getSingleResult();
        return query.getSingleResult().toDomainModel();
    }
}
