package com.knightsofdarkness.storage.kingdom;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.knightsofdarkness.common.kingdom.BuildingName;
import com.knightsofdarkness.common.kingdom.CarriersOnTheMoveDto;
import com.knightsofdarkness.common.kingdom.KingdomDto;
import com.knightsofdarkness.common.kingdom.KingdomSpecialBuildingDto;
import com.knightsofdarkness.common.kingdom.KingdomTurnReport;
import com.knightsofdarkness.common.kingdom.OngoingAttackDto;
import com.knightsofdarkness.common.kingdom.ResourceName;
import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.game.kingdom.KingdomResources;
import com.knightsofdarkness.storage.alliance.AllianceEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Transient;

@Entity
public class KingdomEntity {

    @Id
    @Column(name = "name", nullable = false, unique = true)
    String name;

    @Embedded
    KingdomResourcesEntity resources;
    @Transient
    EnumMap<ResourceName, Integer> resourceMap;

    @Embedded
    KingdomBuildingsEntity buildings;
    @Transient
    EnumMap<BuildingName, Integer> buildingsMap;

    @OneToMany(mappedBy = "kingdom", cascade = CascadeType.ALL, orphanRemoval = true)
    List<KingdomSpecialBuildingEntity> specialBuildings;

    @OneToMany(mappedBy = "kingdom", cascade = CascadeType.ALL, orphanRemoval = true)
    List<KingdomCarriersOnTheMoveEntity> carriersOnTheMove;

    @OneToMany(mappedBy = "kingdom", cascade = CascadeType.ALL, orphanRemoval = true)
    List<KingdomOngoingAttackEntity> ongoingAttacks;

    @Embedded
    KingdomUnitsEntity units;

    @JdbcTypeCode(SqlTypes.JSON)
    KingdomTurnReport lastTurnReport;

    @ManyToOne
    @JoinColumn(name = "alliance_name", nullable = true)
    AllianceEntity alliance;

    public KingdomEntity()
    {
        this.resources = new KingdomResourcesEntity();
        loadResources();
    }

    public KingdomEntity(String name, KingdomResourcesEntity resources, KingdomBuildingsEntity buildings, List<KingdomSpecialBuildingEntity> specialBuildings, List<KingdomCarriersOnTheMoveEntity> carriersOnTheMove,
            List<KingdomOngoingAttackEntity> ongoingAttacks, KingdomUnitsEntity units, KingdomTurnReport lastTurnReport)
    {
        this.name = name;
        this.resources = resources;
        this.buildings = buildings;
        this.specialBuildings = specialBuildings;
        this.carriersOnTheMove = carriersOnTheMove;
        this.ongoingAttacks = ongoingAttacks;
        this.units = units;
        this.lastTurnReport = lastTurnReport;
        loadResources();
    }

    public KingdomEntity(String name, Map<ResourceName, Integer> resources)
    {
        this.name = name;
        this.resources = new KingdomResourcesEntity(resources);
        loadResources();
    }

    public Kingdom toDomainModel(GameConfig gameConfig)
    {
        var specialBuildings = this.specialBuildings.stream().map(KingdomSpecialBuildingEntity::toDomainModel).collect(Collectors.toList());
        var carriersOnTheMove = this.carriersOnTheMove.stream().map(KingdomCarriersOnTheMoveEntity::toDomainModel).collect(Collectors.toList());
        var ongoingAttacks = this.ongoingAttacks.stream().map(KingdomOngoingAttackEntity::toDomainModel).collect(Collectors.toList());
        return new Kingdom(name, gameConfig, new KingdomResources(resources.toEnumMap()), buildings.toDomainModel(), specialBuildings, carriersOnTheMove, ongoingAttacks, units.toDomainModel(), lastTurnReport);
    }

    public KingdomDto toDto()
    {
        List<KingdomSpecialBuildingDto> specialBuildings = this.specialBuildings.stream().map(KingdomSpecialBuildingEntity::toDto).collect(Collectors.toList());
        List<CarriersOnTheMoveDto> carriersOnTheMove = this.carriersOnTheMove.stream().map(KingdomCarriersOnTheMoveEntity::toDto).collect(Collectors.toList());
        List<OngoingAttackDto> ongoingAttacks = this.ongoingAttacks.stream().map(KingdomOngoingAttackEntity::toDto).collect(Collectors.toList());
        return new KingdomDto(name, resources.toDto(), buildings.toDto(), units.toDto(), new ArrayList<>(), specialBuildings, lastTurnReport, carriersOnTheMove, ongoingAttacks);
    }

    public static KingdomEntity fromDomainModel(Kingdom kingdom)
    {
        var kingdomEntity = new KingdomEntity(
                kingdom.getName(),
                new KingdomResourcesEntity(kingdom.getResources()),
                KingdomBuildingsEntity.fromDomainModel(kingdom.getBuildings()),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                KingdomUnitsEntity.fromDomainModel(kingdom.getUnits()),
                kingdom.getLastTurnReport());

        var specialBuildings = kingdom.getSpecialBuildings().stream().map(specialBuilding -> KingdomSpecialBuildingEntity.fromDomainModel(specialBuilding, kingdomEntity)).toList();
        kingdomEntity.specialBuildings = specialBuildings;

        var carriersOnTheMoveEntities = kingdom.getCarriersOnTheMove().stream().map(carriersOnTheMove -> KingdomCarriersOnTheMoveEntity.fromDomainModel(carriersOnTheMove, kingdomEntity)).toList();
        kingdomEntity.carriersOnTheMove = carriersOnTheMoveEntities;

        var ongoingAttackEntities = kingdom.getOngoingAttacks().stream().map(ongoingAttack -> KingdomOngoingAttackEntity.fromDomainModel(ongoingAttack, kingdomEntity)).toList();
        kingdomEntity.ongoingAttacks = ongoingAttackEntities;

        return kingdomEntity;
    }

    public static KingdomEntity fromDto(KingdomDto dto)
    {
        var kingdomEntity = new KingdomEntity(
                dto.name,
                new KingdomResourcesEntity(dto.resources),
                KingdomBuildingsEntity.fromDto(dto.buildings),
                new ArrayList<>(),
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

    @PrePersist
    @PreUpdate
    public void syncResources()
    {
        resources.loadMap(resourceMap);
    }

    @PostLoad
    public void loadResources()
    {
        resourceMap = resources.toEnumMap();
    }
}
