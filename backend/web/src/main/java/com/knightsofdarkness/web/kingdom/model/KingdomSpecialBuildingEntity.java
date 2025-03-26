package com.knightsofdarkness.web.kingdom.model;

import java.util.UUID;

import com.knightsofdarkness.web.common.kingdom.KingdomSpecialBuildingDto;
import com.knightsofdarkness.web.common.kingdom.SpecialBuildingType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class KingdomSpecialBuildingEntity {
    @Id
    UUID id;

    @ManyToOne
    @JoinColumn(name = "kingdom_name", nullable = false)
    KingdomEntity kingdom;

    @Enumerated(EnumType.STRING)
    SpecialBuildingType buildingType;

    int level;

    int buildingPointsPut;

    int buildingPointsRequired;

    boolean isMaxLevel;

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

    public KingdomSpecialBuildingDto toDto()
    {
        return new KingdomSpecialBuildingDto(id, buildingType, level, buildingPointsPut, buildingPointsRequired, isMaxLevel);
    }

    public static KingdomSpecialBuildingEntity fromDto(KingdomSpecialBuildingDto kingdomSpecialBuildingDto, KingdomEntity kingdomEntity)
    {
        return new KingdomSpecialBuildingEntity(kingdomSpecialBuildingDto.id(), kingdomEntity, kingdomSpecialBuildingDto.buildingType(), kingdomSpecialBuildingDto.level(), kingdomSpecialBuildingDto.buildingPointsPut(),
                kingdomSpecialBuildingDto.buildingPointsRequired(), kingdomSpecialBuildingDto.isMaxLevel());
    }

    public boolean isMaxLevel()
    {
        return level >= 5;
    }

    public UUID getId()
    {
        return id;
    }
}
