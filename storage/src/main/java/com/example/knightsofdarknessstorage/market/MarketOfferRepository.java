package com.example.knightsofdarknessstorage.market;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.knightsofdarkness.game.market.MarketOffer;
import com.knightsofdarkness.game.storage.IMarketOfferRepository;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@Repository
public class MarketOfferRepository implements IMarketOfferRepository {

    private final EntityManager entityManager;

    public MarketOfferRepository(EntityManager entityManager)
    {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public MarketOffer save(MarketOffer marketOffer)
    {
        entityManager.persist(marketOffer);
        return marketOffer;
    }

    @Override
    @Transactional
    public void deleteById(long marketOfferId)
    {
        MarketOffer marketOffer = findById(marketOfferId);
        entityManager.remove(marketOffer);
    }

    @Override
    public MarketOffer findById(long marketOfferId)
    {
        return entityManager.find(MarketOfferEntity.class, marketOfferId).toDomainModel();
    }

    @Override
    public List<MarketOffer> findAll()
    {
        return entityManager.createQuery("SELECT s FROM Student s", MarketOfferEntity.class).getResultList().stream().map(MarketOfferEntity::toDomainModel).toList();
    }
}
