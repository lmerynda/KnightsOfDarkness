package com.knightsofdarkness.storage.kingdom;

import java.util.ArrayList;
import java.util.List;

import com.knightsofdarkness.game.gameconfig.GameConfig;
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

    @Embedded
    KingdomBuildingsEntity buildings;

    @Embedded
    KingdomUnitsEntity units;

    @OneToMany(mappedBy = "kingdom", cascade = CascadeType.ALL, orphanRemoval = true)
    List<MarketOfferEntity> marketOffers;

    public KingdomEntity()
    {
    }

    public KingdomEntity(String name, KingdomResourcesEntity resources, KingdomBuildingsEntity buildings, KingdomUnitsEntity units, List<MarketOfferEntity> marketOffers)
    {
        this.name = name;
        this.resources = resources;
        this.buildings = buildings;
        this.units = units;
        this.marketOffers = marketOffers;
    }

    public Kingdom toDomainModel(GameConfig gameConfig)
    {
        // TODO fix nulls
        return new Kingdom(name, gameConfig, null, resources.toDomainModel(), buildings.toDomainModel(), units.toDomainModel());
    }

    public static KingdomEntity fromDomainModel(Kingdom kingdom)
    {
        return new KingdomEntity(kingdom.getName(), KingdomResourcesEntity.frommDomainModel(kingdom.getResources()), KingdomBuildingsEntity.frommDomainModel(kingdom.getBuildings()), KingdomUnitsEntity.fromDomainModel(kingdom.getUnits()),
                new ArrayList<>());
    }
}
