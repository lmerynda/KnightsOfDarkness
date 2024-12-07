package com.knightsofdarkness.storage.kingdom;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.knightsofdarkness.common.kingdom.CarriersOnTheMoveDto;
import com.knightsofdarkness.common.kingdom.KingdomDto;
import com.knightsofdarkness.common.kingdom.KingdomResourcesDto;
import com.knightsofdarkness.common.kingdom.KingdomSpecialBuildingDto;
import com.knightsofdarkness.common.kingdom.KingdomTurnReport;
import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.game.kingdom.KingdomResources;

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

    @JdbcTypeCode(SqlTypes.JSON)
    KingdomResourcesDto resources;

    @Embedded
    KingdomBuildingsEntity buildings;

    @OneToMany(mappedBy = "kingdom", cascade = CascadeType.ALL, orphanRemoval = true)
    List<KingdomSpecialBuildingEntity> specialBuildings;

    @OneToMany(mappedBy = "kingdom", cascade = CascadeType.ALL, orphanRemoval = true)
    List<KingdomCarriersOnTheMoveEntity> carriersOnTheMove;

    @Embedded
    KingdomUnitsEntity units;

    @JdbcTypeCode(SqlTypes.JSON)
    KingdomTurnReport lastTurnReport;

    public KingdomEntity()
    {
    }

    public KingdomEntity(String name, KingdomResourcesDto resources, KingdomBuildingsEntity buildings, List<KingdomSpecialBuildingEntity> specialBuildings, List<KingdomCarriersOnTheMoveEntity> carriersOnTheMove, KingdomUnitsEntity units,
            KingdomTurnReport lastTurnReport)
    {
        this.name = name;
        this.resources = resources;
        this.buildings = buildings;
        this.specialBuildings = specialBuildings;
        this.carriersOnTheMove = carriersOnTheMove;
        this.units = units;
        this.lastTurnReport = lastTurnReport;
    }

    public Kingdom toDomainModel(GameConfig gameConfig)
    {
        var specialBuildings = this.specialBuildings.stream().map(KingdomSpecialBuildingEntity::toDomainModel).collect(Collectors.toList());
        var carriersOnTheMove = this.carriersOnTheMove.stream().map(KingdomCarriersOnTheMoveEntity::toDomainModel).collect(Collectors.toList());
        return new Kingdom(name, gameConfig, new KingdomResources(resources.getResources()), buildings.toDomainModel(), specialBuildings, carriersOnTheMove, units.toDomainModel(), lastTurnReport);
    }

    public KingdomDto toDto()
    {
        List<KingdomSpecialBuildingDto> specialBuildings = this.specialBuildings.stream().map(KingdomSpecialBuildingEntity::toDto).collect(Collectors.toList());
        List<CarriersOnTheMoveDto> carriersOnTheMove = this.carriersOnTheMove.stream().map(KingdomCarriersOnTheMoveEntity::toDto).collect(Collectors.toList());
        return new KingdomDto(name, resources, buildings.toDto(), units.toDto(), new ArrayList<>(), specialBuildings, lastTurnReport, carriersOnTheMove);
    }

    public static KingdomEntity fromDomainModel(Kingdom kingdom)
    {
        var kingdomEntity = new KingdomEntity(
                kingdom.getName(),
                new KingdomResourcesDto(kingdom.getResources().getResources()),
                KingdomBuildingsEntity.fromDomainModel(kingdom.getBuildings()),
                new ArrayList<>(),
                new ArrayList<>(),
                KingdomUnitsEntity.fromDomainModel(kingdom.getUnits()),
                kingdom.getLastTurnReport());

        var specialBuildings = kingdom.getSpecialBuildings().stream().map(specialBuilding -> KingdomSpecialBuildingEntity.fromDomainModel(specialBuilding, kingdomEntity)).toList();
        kingdomEntity.specialBuildings = specialBuildings;

        var carriersOnTheMoveEntities = kingdom.getCarriersOnTheMove().stream().map(carriersOnTheMove -> KingdomCarriersOnTheMoveEntity.fromDomainModel(carriersOnTheMove, kingdomEntity)).toList();
        kingdomEntity.carriersOnTheMove = carriersOnTheMoveEntities;

        return kingdomEntity;
    }

    public static KingdomEntity fromDto(KingdomDto dto)
    {
        var kingdomEntity = new KingdomEntity(
                dto.name,
                dto.resources,
                KingdomBuildingsEntity.fromDto(dto.buildings),
                new ArrayList<>(),
                new ArrayList<>(),
                KingdomUnitsEntity.fromDto(dto.units),
                dto.lastTurnReport);

        var specialBuildings = dto.specialBuildings.stream().map(specialBuilding -> KingdomSpecialBuildingEntity.fromDto(specialBuilding, kingdomEntity)).toList();
        kingdomEntity.specialBuildings = specialBuildings;

        return kingdomEntity;
    }

    public String getName()
    {
        return name;
    }
}
