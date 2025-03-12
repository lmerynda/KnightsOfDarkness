package com.knightsofdarkness.web.kingdom.model;

import java.util.Optional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
import com.knightsofdarkness.common.market.MarketResource;
import com.knightsofdarkness.web.alliance.model.AllianceEntity;
import com.knightsofdarkness.web.game.config.GameConfig;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
    }

    public KingdomDto toDto()
    {
        List<KingdomSpecialBuildingDto> specialBuildings = this.specialBuildings.stream().map(KingdomSpecialBuildingEntity::toDto).collect(Collectors.toList());
        List<CarriersOnTheMoveDto> carriersOnTheMove = this.carriersOnTheMove.stream().map(KingdomCarriersOnTheMoveEntity::toDto).collect(Collectors.toList());
        List<OngoingAttackDto> ongoingAttacks = this.ongoingAttacks.stream().map(KingdomOngoingAttackEntity::toDto).collect(Collectors.toList());
        return new KingdomDto(name, resources.toDto(), buildings.toDto(), units.toDto(), new ArrayList<>(), specialBuildings, lastTurnReport, carriersOnTheMove, ongoingAttacks);
    }

    public static KingdomEntity fromDto(KingdomDto dto)
    {
        var kingdomEntity = new KingdomEntity(
                dto.name,
                new KingdomResourcesEntity(dto.resources.getResources()),
                new KingdomBuildingsEntity(dto.buildings.getBuildings()),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new KingdomUnitsEntity(dto.units.getAvailableUnits().getUnits(), dto.units.getMobileUnits().getUnits()),
                dto.lastTurnReport);

        var specialBuildings = dto.specialBuildings.stream().map(specialBuilding -> KingdomSpecialBuildingEntity.fromDto(specialBuilding, kingdomEntity)).toList();
        kingdomEntity.specialBuildings = specialBuildings;

        return kingdomEntity;
    }

    public String getName()
    {
        return name;
    }

    public KingdomResourcesEntity getResources()
    {
        return resources;
    }

    public int getBuildingCapacity(BuildingName name, GameConfig config)
    {
        return buildings.getCapacity(name, config.buildingCapacity().getCapacity(name));
    }

    public void addTurn()
    {
        resources.addCount(ResourceName.turns, 1);
    }

    public int getTotalPeopleCount()
    {
        return units.countAll() + resources.getCount(ResourceName.unemployed);
    }

    public int getUnusedLand()
    {
        return resources.getCount(ResourceName.land) - buildings.countAll();
    }

    public int getOccupiedLand()
    {
        return buildings.countAll();
    }

    public KingdomBuildingsEntity getBuildings()
    {
        return buildings;
    }

    public KingdomUnitsEntity getUnits()
    {
        return units;
    }

    public KingdomTurnReport getLastTurnReport()
    {
        return lastTurnReport;
    }

    public List<KingdomSpecialBuildingEntity> getSpecialBuildings()
    {
        return specialBuildings;
    }

    public List<KingdomCarriersOnTheMoveEntity> getCarriersOnTheMove()
    {
        return carriersOnTheMove;
    }

    public List<KingdomOngoingAttackEntity> getOngoingAttacks()
    {
        return ongoingAttacks;
    }

    // TODO do it directly?
    public void receiveResourceTransfer(MarketResource resource, int amount)
    {
        resources.addCount(ResourceName.from(resource), amount);
    }

    public void setAlliance(AllianceEntity allianceEntity)
    {
        this.alliance = allianceEntity;
    }

    public void removeAlliance()
    {
        assert this.alliance != null;
        this.alliance.getKingdoms().remove(this);
        this.alliance = null;
    }

    public Optional<AllianceEntity> getAlliance()
    {
        return Optional.ofNullable(alliance);
    }

    // it's really hard to make this commit...
    @Override
    public boolean equals(Object other)
    {
        if (this == other)
            return true;
        if (other == null || getClass() != other.getClass())
            return false;
        KingdomEntity that = (KingdomEntity) other;
        return Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name);
    }
}
