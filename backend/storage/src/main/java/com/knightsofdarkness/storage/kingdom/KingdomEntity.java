package com.knightsofdarkness.storage.kingdom;

import java.util.ArrayList;
import java.util.List;

import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.storage.market.MarketOfferEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class KingdomEntity {

    @Id
    @Column(name = "name", nullable = false, unique = true)
    String name;

    @Embedded
    KingdomResourcesEntity resources;

    @OneToMany(mappedBy = "kingdom", cascade = CascadeType.ALL, orphanRemoval = true)
    List<MarketOfferEntity> marketOffers;

    public KingdomEntity()
    {
    }

    public KingdomEntity(String name, KingdomResourcesEntity resources, List<MarketOfferEntity> marketOffers)
    {
        this.name = name;
        this.resources = resources;
        this.marketOffers = marketOffers;
    }

    public Kingdom toDomainModel()
    {
        // TODO fix
        return new Kingdom(name, null, null, resources.toDomainModel(), null, null);
    }

    public static KingdomEntity fromDomainModel(Kingdom kingdom)
    {
        return new KingdomEntity(kingdom.getName(), KingdomResourcesEntity.frommDomainModel(kingdom.getResources()), new ArrayList<>());
    }
}
