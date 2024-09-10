package com.knightsofdarkness.storage.kingdom;

import java.util.UUID;

import com.knightsofdarkness.common_legacy.kingdom.KingdomSpecialBuildingDto;
import com.knightsofdarkness.game.kingdom.KingdomSpecialBuilding;
import com.knightsofdarkness.game.kingdom.SpecialBuildingType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class KingdomSpecialBuildingEntity {
    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "kingdom_name", nullable = false)
    private KingdomEntity kingdom;

    @Enumerated(EnumType.STRING)
    private SpecialBuildingType buildingType;

    private int level;

    private int buildingPointsPut;

    private int buildingPointsRequired;

    private boolean isMaxLevel;

    public KingdomSpecialBuildingEntity()
    {
    }

    public KingdomSpecialBuildingEntity(UUID id, KingdomEntity kingdom, SpecialBuildingType buildingType, int level, int buildingPointsPut, int buildingPointsRequired, boolean isMaxLevel)
    {
        this.id = id;
        this.kingdom = kingdom;
        this.buildingType = buildingType;
        this.level = level;
        this.buildingPointsPut = buildingPointsPut;
        this.buildingPointsRequired = buildingPointsRequired;
        this.isMaxLevel = isMaxLevel;
    }

    public KingdomSpecialBuilding toDomainModel()
    {
        return new KingdomSpecialBuilding(id, buildingType, level, buildingPointsPut, buildingPointsRequired, isMaxLevel);
    }

    public static KingdomSpecialBuildingEntity fromDomainModel(KingdomSpecialBuilding kingdomSpecialBuilding, KingdomEntity kingdomEntity)
    {
        return new KingdomSpecialBuildingEntity(kingdomSpecialBuilding.getId(), kingdomEntity, kingdomSpecialBuilding.getBuildingType(), kingdomSpecialBuilding.getLevel(), kingdomSpecialBuilding.getBuildingPointsPut(),
                kingdomSpecialBuilding.getBuildingPointsRequired(), kingdomSpecialBuilding.isMaxLevel());
    }

    public KingdomSpecialBuildingDto toDto()
    {
        return new KingdomSpecialBuildingDto(id, buildingType, level, buildingPointsPut, buildingPointsRequired, isMaxLevel);
    }
}
