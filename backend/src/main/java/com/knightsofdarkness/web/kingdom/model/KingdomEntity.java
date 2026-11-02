package com.knightsofdarkness.web.kingdom.model;

import java.util.Optional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.knightsofdarkness.web.alliance.model.AllianceEntity;
import com.knightsofdarkness.web.common.kingdom.BuildingName;
import com.knightsofdarkness.web.common.kingdom.CarriersOnTheMoveDto;
import com.knightsofdarkness.web.common.kingdom.KingdomDto;
import com.knightsofdarkness.web.common.kingdom.KingdomSpecialBuildingDto;
import com.knightsofdarkness.web.common.kingdom.KingdomTurnReport;
import com.knightsofdarkness.web.common.kingdom.OngoingAttackDto;
import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.market.MarketResource;
import com.knightsofdarkness.web.game.config.GameConfig;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Version;

@Entity
public class KingdomEntity
{
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

    /**
     * our soldiers on the way to, or standing guard in, allied kingdoms
     */
    @OneToMany(mappedBy = "kingdom", cascade = CascadeType.ALL, orphanRemoval = true)
    List<KingdomReinforcementEntity> reinforcements = new ArrayList<>();

    /**
     * allied soldiers on the way to, or standing guard in, this kingdom
     */
    @OneToMany(mappedBy = "host")
    List<KingdomReinforcementEntity> garrisons = new ArrayList<>();

    @Embedded
    KingdomUnitsEntity units;

    @JdbcTypeCode(SqlTypes.JSON)
    KingdomTurnReport lastTurnReport;

    @ManyToOne
    @JoinColumn(name = "alliance_name", nullable = true)
    AllianceEntity alliance;

    /**
     * world ticks left during which nobody may send an army against this kingdom, granted when its defense was broken
     */
    int protectionTicks;

    /**
     * two writers changing the same kingdom at once (a player, a battle, a bot) must not silently overwrite each other
     */
    @Version
    Long version;

    public KingdomEntity()
    {
    }

    @SuppressWarnings("java:S107")
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
        List<KingdomSpecialBuildingDto> specialBuildings = this.specialBuildings.stream().map(KingdomSpecialBuildingEntity::toDto).toList();
        List<CarriersOnTheMoveDto> carriersOnTheMove = this.carriersOnTheMove.stream().map(KingdomCarriersOnTheMoveEntity::toDto).toList();
        List<OngoingAttackDto> ongoingAttacks = this.ongoingAttacks.stream().map(KingdomOngoingAttackEntity::toDto).toList();
        Optional<String> allianceName = alliance != null ? Optional.of(alliance.getName()) : Optional.empty();
        var dto = new KingdomDto(name, resources.toDto(), buildings.toDto(), units.toDto(), new ArrayList<>(), specialBuildings, lastTurnReport, carriersOnTheMove, ongoingAttacks, allianceName);
        dto.protectionTicks = protectionTicks;
        dto.reinforcements = reinforcements.stream().map(KingdomReinforcementEntity::toDto).toList();
        dto.garrisons = garrisons.stream().map(KingdomReinforcementEntity::toDto).toList();
        return dto;
    }

    public static KingdomEntity fromDto(KingdomDto dto)
    {
        var kingdomEntity = new KingdomEntity(
                dto.name,
                new KingdomResourcesEntity(dto.resources),
                new KingdomBuildingsEntity(dto.buildings),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new KingdomUnitsEntity(dto.units.getAvailableUnits(), dto.units.getMobileUnits()),
                dto.lastTurnReport);

        kingdomEntity.specialBuildings.addAll(dto.specialBuildings.stream().map(specialBuilding -> KingdomSpecialBuildingEntity.fromDto(specialBuilding, kingdomEntity)).toList());

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

    public void resetToStart(GameConfig config)
    {
        var start = config.kingdomStartConfiguration();
        var startingUnits = start.units().toDto();
        resources = new KingdomResourcesEntity(start.resources().toMap());
        buildings = new KingdomBuildingsEntity(start.buildings().toMap());
        units = new KingdomUnitsEntity(startingUnits.getAvailableUnits(), startingUnits.getMobileUnits());
        specialBuildings.clear();
        carriersOnTheMove.clear();
        ongoingAttacks.clear();
        reinforcements.clear();
        garrisons.clear();
        lastTurnReport = new KingdomTurnReport();
    }

    public int getTotalPeopleCount()
    {
        return units.countAll() + resources.getCount(ResourceName.unemployed);
    }

    /**
     * soldiers live in barracks, everyone else needs a house
     */
    public int getHousedPeopleCount()
    {
        return getTotalPeopleCount() - units.countAllMilitary();
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
    public void receiveResourceTransfer(MarketResource resource, int count)
    {
        resources.addCount(ResourceName.from(resource), count);
    }

    public void setAlliance(AllianceEntity allianceEntity)
    {
        this.alliance = allianceEntity;
    }

    public void removeAlliance()
    {
        assert this.alliance != null;
        sendGarrisonsHome();
        this.alliance.getKingdoms().remove(this);
        this.alliance = null;
    }

    /**
     * soldiers stand guard only between allies: ours abroad and allies' here all go home
     */
    void sendGarrisonsHome()
    {
        for (var reinforcement : List.copyOf(reinforcements))
        {
            reinforcement.goHome();
        }
        for (var garrison : List.copyOf(garrisons))
        {
            garrison.goHome();
        }
    }

    public List<KingdomReinforcementEntity> getReinforcements()
    {
        return reinforcements;
    }

    public List<KingdomReinforcementEntity> getGarrisons()
    {
        return garrisons;
    }

    /**
     * allied soldiers that have arrived and fight beside this kingdom's own
     */
    public List<KingdomReinforcementEntity> getStandingGarrisons()
    {
        return garrisons.stream().filter(KingdomReinforcementEntity::isStanding).toList();
    }

    public Optional<AllianceEntity> getAlliance()
    {
        return Optional.ofNullable(alliance);
    }

    public int getProtectionTicks()
    {
        return protectionTicks;
    }

    public void setProtectionTicks(int protectionTicks)
    {
        this.protectionTicks = protectionTicks;
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
