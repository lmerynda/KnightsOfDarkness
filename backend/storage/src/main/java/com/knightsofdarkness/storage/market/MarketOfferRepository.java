package com.knightsofdarkness.storage.market;

import java.util.Optional;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.game.market.MarketOffer;
import com.knightsofdarkness.game.market.MarketResource;
import com.knightsofdarkness.game.storage.IMarketOfferRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

@Repository
public class MarketOfferRepository implements IMarketOfferRepository {

    @Autowired
    private GameConfig gameConfig;

    @Autowired
    private EntityManager entityManager;

    @Override
    public MarketOffer add(MarketOffer marketOffer)
    {
        entityManager.persist(MarketOfferEntity.fromDomainModel(marketOffer));

        return marketOffer;
    }

    @Override
    public void remove(MarketOffer marketOffer)
    {
        entityManager.remove(marketOffer);
    }

    @Override
    public List<MarketOffer> getOffersByResource(MarketResource resource)
    {
        TypedQuery<MarketOfferEntity> query = entityManager.createQuery("SELECT offer FROM MarketOfferEntity offer WHERE offer.resource = :resource", MarketOfferEntity.class);
        query.setParameter("resource", resource);
        return query.getResultList().stream().map(marketOfferEntity -> marketOfferEntity.toDomainModel(gameConfig)).toList();
    }

    @Override
    public Optional<MarketOffer> getCheapestOfferByResource(MarketResource resource)
    {
        throw new UnsupportedOperationException("Not implemented");
        // TypedQuery<MarketOffer> query = entityManager.createQuery("SELECT offer FROM MarketOffer offer WHERE offer.resource = :resource ORDER BY offer.price ASC", MarketOffer.class);
        // query.setParameter("resource", resource);
        // query.setMaxResults(1);
        // var results = query.getResultList();
        // return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public List<MarketOffer> getOffersByKingdomName(String name)
    {
        throw new UnsupportedOperationException("Not implemented");
        // TypedQuery<MarketOffer> query = entityManager.createQuery("SELECT offer FROM MarketOffer offer WHERE offer.kingdom.id = :kingdomId", MarketOffer.class);
        // query.setParameter("kingdomId", kingdomId);
        // return query.getResultList();
    }

    @Override
    public Optional<MarketOffer> findById(UUID marketOfferId)
    {
        TypedQuery<MarketOfferEntity> query = entityManager.createQuery("SELECT offer FROM MarketOfferEntity offer WHERE offer.id = :id", MarketOfferEntity.class);
        query.setParameter("id", marketOfferId);
        var offer = query.getSingleResult();
        return Optional.ofNullable(offer).map(marketOfferEntity -> marketOfferEntity.toDomainModel(gameConfig));
    }

    public void update(MarketOffer marketOffer)
    {
        var marketOfferEntity = MarketOfferEntity.fromDomainModel(marketOffer);
        // seriously, ask MM why it cannot be the other way around - cascading issue?
        entityManager.merge(marketOfferEntity.getKingdom());
        entityManager.merge(marketOfferEntity);
    }
}
