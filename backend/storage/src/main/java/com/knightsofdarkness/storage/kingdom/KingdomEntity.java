package com.knightsofdarkness.storage.kingdom;

import java.util.ArrayList;
import java.util.List;

import com.knightsofdarkness.common.KingdomDto;
import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.storage.market.MarketOfferEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

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

    public KingdomEntity()
    {
    }

    public KingdomEntity(String name, KingdomResourcesEntity resources, KingdomBuildingsEntity buildings, KingdomUnitsEntity units, List<MarketOfferEntity> marketOffers)
    {
        this.name = name;
        this.resources = resources;
        this.buildings = buildings;
        this.units = units;
    }

    public Kingdom toDomainModel(GameConfig gameConfig)
    {
        var kingdom = new Kingdom(name, gameConfig, resources.toDomainModel(), buildings.toDomainModel(), units.toDomainModel(), new ArrayList<>());
        return kingdom;
    }

    public KingdomDto toDto()
    {
        return new KingdomDto(name, resources.toDto(), buildings.toDto(), units.toDto(), new ArrayList<>());
    }

    public static KingdomEntity fromDomainModel(Kingdom kingdom)
    {
        var kingdomEntity = new KingdomEntity(kingdom.getName(), KingdomResourcesEntity.fromDomainModel(kingdom.getResources()), KingdomBuildingsEntity.fromDomainModel(kingdom.getBuildings()), KingdomUnitsEntity.fromDomainModel(kingdom.getUnits()), new ArrayList<>());
        return kingdomEntity;
    }

    public String getName()
    {
        return name;
    }
}
