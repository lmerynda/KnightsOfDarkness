package com.knightsofdarkness.storage.kingdom;

import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.knightsofdarkness.common.kingdom.BuildingName;
import com.knightsofdarkness.common.kingdom.KingdomBuildingsDto;
import com.knightsofdarkness.game.kingdom.KingdomBuildings;

import jakarta.persistence.Embeddable;

@Embeddable
class KingdomBuildingsEntity {
    @JdbcTypeCode(SqlTypes.JSON)
    public Map<BuildingName, Integer> buildings;

    public KingdomBuildingsEntity()
    {
    }

    public KingdomBuildingsEntity(Map<BuildingName, Integer> buildings)
    {
        this.buildings = buildings;
    }

    public KingdomBuildingsDto toDto()
    {
        return new KingdomBuildingsDto(buildings);
    }

    public static KingdomBuildingsEntity fromDto(KingdomBuildingsDto dto)
    {
        return new KingdomBuildingsEntity(dto.getBuildings());
    }

    public KingdomBuildings toDomainModel()
    {
        return new KingdomBuildings(buildings);
    }

    public static KingdomBuildingsEntity fromDomainModel(KingdomBuildings buildings)
    {
        return new KingdomBuildingsEntity(buildings.getBuildings());
    }
}
