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
        return jpaRepository.findFirstByResourceOrderByPriceAsc(resource).map(marketOfferEntity -> marketOfferEntity.toDomainModel(gameConfig));
    }

    @Override
    public List<MarketOffer> getOffersByKingdomName(String name)
    {
        return jpaRepository.findByKingdomName(name).stream().map(marketOfferEntity -> marketOfferEntity.toDomainModel(gameConfig)).toList();
    }

    @Override
    public Optional<MarketOffer> findById(UUID marketOfferId)
    {
        var marketOffer = jpaRepository.findById(marketOfferId);
        return marketOffer.map(marketOfferEntity -> marketOfferEntity.toDomainModel(gameConfig));
    }

    public void update(MarketOffer marketOffer)
    {
        var marketOfferEntity = MarketOfferEntity.fromDomainModel(marketOffer);
        jpaRepository.save(marketOfferEntity);
    }
}
