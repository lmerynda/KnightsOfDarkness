package com.knightsofdarkness.storage.market;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.knightsofdarkness.game.market.MarketOffer;
import com.knightsofdarkness.game.market.MarketResource;
import com.knightsofdarkness.game.storage.IMarketOfferRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

@Repository
public class MarketOfferRepository implements IMarketOfferRepository {

    private final EntityManager entityManager;

    public MarketOfferRepository(EntityManager entityManager)
    {
        this.entityManager = entityManager;
    }

    @Override
    public MarketOffer add(MarketOffer marketOffer)
    {
        entityManager.persist(marketOffer);

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
        return query.getResultList().stream().map(MarketOfferEntity::toDomainModel).toList();
    }

    @Override
    public Optional<MarketOffer> getCheapestOfferByResource(MarketResource resource)
    {
        TypedQuery<MarketOffer> query = entityManager.createQuery("SELECT offer FROM MarketOffer offer WHERE offer.resource = :resource ORDER BY offer.price ASC", MarketOffer.class);
        query.setParameter("resource", resource);
        query.setMaxResults(1);
        var results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public List<MarketOffer> getOffersByKingdomId(Long kingdomId)
    {
        TypedQuery<MarketOffer> query = entityManager.createQuery("SELECT offer FROM MarketOffer offer WHERE offer.kingdom.id = :kingdomId", MarketOffer.class);
        query.setParameter("kingdomId", kingdomId);
        return query.getResultList();
    }

    @Override
    public Optional<MarketOffer> findById(long marketOfferId)
    {
        TypedQuery<MarketOffer> query = entityManager.createQuery("SELECT offer FROM MarketOffer offer WHERE offer.id = :id", MarketOffer.class);
        query.setParameter("id", marketOfferId);
        query.setMaxResults(1);
        var results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

}
