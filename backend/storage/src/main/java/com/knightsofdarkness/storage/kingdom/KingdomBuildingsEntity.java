package com.knightsofdarkness.storage.kingdom;

import com.knightsofdarkness.common.GsonFactory;
import com.knightsofdarkness.common.kingdom.KingdomBuildingsDto;
import com.knightsofdarkness.game.kingdom.KingdomBuildings;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
class KingdomBuildingsEntity {
    @Column(name = "buildings", columnDefinition = "TEXT")
    String buildingsJson;

    public KingdomBuildingsEntity()
    {
    }

    public KingdomBuildingsEntity(String buildingsJson)
    {
        this.buildingsJson = buildingsJson;
    }

    public KingdomBuildingsDto toDto()
    {
        return GsonFactory.createGson().fromJson(buildingsJson, KingdomBuildingsDto.class);
    }

    public static KingdomBuildingsEntity fromDto(KingdomBuildingsDto dto)
    {
        String buildingsJson = GsonFactory.createGson().toJson(dto, KingdomBuildingsDto.class);
        return new KingdomBuildingsEntity(buildingsJson);
    }

    public KingdomBuildings toDomainModel()
    {
        var dto = toDto();
        return new KingdomBuildings(dto.getBuildings());
    }

    public static KingdomBuildingsEntity fromDomainModel(KingdomBuildings buildings)
    {
        var dto = new KingdomBuildingsDto(buildings.getBuildings());
        String buildingsJson = GsonFactory.createGson().toJson(dto, KingdomBuildingsDto.class);
        return new KingdomBuildingsEntity(buildingsJson);
    }
}
