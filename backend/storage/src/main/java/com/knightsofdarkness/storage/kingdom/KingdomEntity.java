package com.knightsofdarkness.storage.kingdom;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.knightsofdarkness.common.KingdomDto;
import com.knightsofdarkness.common.KingdomSpecialBuildingDto;
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

    @OneToMany(mappedBy = "kingdom", cascade = CascadeType.ALL)
    List<KingdomSpecialBuildingEntity> specialBuildings;

    @Embedded
    KingdomUnitsEntity units;

    @Embedded
    KingdomTurnReportEntity lastTurnReport;

    public KingdomEntity()
    {
    }

    public KingdomEntity(String name, KingdomResourcesEntity resources, KingdomBuildingsEntity buildings, List<KingdomSpecialBuildingEntity> specialBuildings, KingdomUnitsEntity units, List<MarketOfferEntity> marketOffers,
            KingdomTurnReportEntity lastTurnReport)
    {
        this.name = name;
        this.resources = resources;
        this.buildings = buildings;
        this.specialBuildings = specialBuildings;
        this.units = units;
        this.lastTurnReport = lastTurnReport;
    }

    public Kingdom toDomainModel(GameConfig gameConfig)
    {
        var specialBuildings = this.specialBuildings.stream().map(KingdomSpecialBuildingEntity::toDomainModel).collect(Collectors.toList());
        var kingdom = new Kingdom(name, gameConfig, resources.toDomainModel(), buildings.toDomainModel(), specialBuildings, units.toDomainModel(), lastTurnReport.toDomainModel());
        return kingdom;
    }

    public KingdomDto toDto()
    {
        List<KingdomSpecialBuildingDto> specialBuildings = this.specialBuildings.stream().map(KingdomSpecialBuildingEntity::toDto).collect(Collectors.toList());
        return new KingdomDto(name, resources.toDto(), buildings.toDto(), units.toDto(), new ArrayList<>(), specialBuildings, lastTurnReport.toDto());
    }

    public static KingdomEntity fromDomainModel(Kingdom kingdom)
    {
        var kingdomEntity = new KingdomEntity(
                kingdom.getName(),
                KingdomResourcesEntity.fromDomainModel(kingdom.getResources()),
                KingdomBuildingsEntity.fromDomainModel(kingdom.getBuildings()),
                new ArrayList<>(),
                KingdomUnitsEntity.fromDomainModel(kingdom.getUnits()),
                new ArrayList<>(),
                KingdomTurnReportEntity.fromDomainModel(kingdom.getLastTurnReport()));

        var specialBuildings = kingdom.getSpecialBuildings().stream().map(specialBuilding -> KingdomSpecialBuildingEntity.fromDomainModel(specialBuilding, kingdomEntity)).toList();
        kingdomEntity.specialBuildings = specialBuildings;

        return kingdomEntity;
    }

    public String getName()
    {
        return name;
    }
}
