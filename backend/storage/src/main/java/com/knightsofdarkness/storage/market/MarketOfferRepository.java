package com.knightsofdarkness.storage.market;

import java.util.Optional;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.game.market.MarketOffer;
import com.knightsofdarkness.game.market.MarketResource;
import com.knightsofdarkness.game.storage.IMarketOfferRepository;

@Repository
public class MarketOfferRepository implements IMarketOfferRepository {

    private final GameConfig gameConfig;
    private final MarketOfferJpaRepository jpaRepository;

    public MarketOfferRepository(GameConfig gameConfig, MarketOfferJpaRepository jpaRepository)
    {
        this.gameConfig = gameConfig;
        this.jpaRepository = jpaRepository;
    }

    @Override
    public MarketOffer add(MarketOffer marketOffer)
    {
        var marketOfferEntity = jpaRepository.save(MarketOfferEntity.fromDomainModel(marketOffer));

        return marketOfferEntity.toDomainModel(gameConfig);
    }

    @Override
    public void remove(MarketOffer marketOffer)
    {
        var marketOfferEntity = MarketOfferEntity.fromDomainModel(marketOffer);
        jpaRepository.delete(marketOfferEntity);
    }

    @Override
    public List<MarketOffer> getOffersByResource(MarketResource resource)
    {
        var offers = jpaRepository.findByResource(resource);
        return offers.stream().map(marketOfferEntity -> marketOfferEntity.toDomainModel(gameConfig)).toList();
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
        return jpaRepository.findById(marketOfferId).map(marketOfferEntity -> marketOfferEntity.toDomainModel(gameConfig));
    }

    public void update(MarketOffer marketOffer)
    {
        var marketOfferEntity = MarketOfferEntity.fromDomainModel(marketOffer);
        // seriously, ask MM why it cannot be the other way around - cascading issue?
        // jpaRepository.update(marketOfferEntity.getKingdom());
        jpaRepository.save(marketOfferEntity);
        // entityManager.merge(marketOfferEntity.getKingdom()); // TODO, make sure kingdom gets updates and remove this comment

    }
}
