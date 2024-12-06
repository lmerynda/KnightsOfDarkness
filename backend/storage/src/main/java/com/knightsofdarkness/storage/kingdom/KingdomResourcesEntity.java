package com.knightsofdarkness.storage.kingdom;

import com.knightsofdarkness.common.GsonFactory;
import com.knightsofdarkness.common.kingdom.KingdomResourcesDto;
import com.knightsofdarkness.game.kingdom.KingdomResources;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
class KingdomResourcesEntity {
    @Column(name = "resources", columnDefinition = "TEXT")
    String resourcesJson;

    public KingdomResourcesEntity()
    {
    }

    public KingdomResourcesEntity(String resourcesJson)
    {
        this.resourcesJson = resourcesJson;
    }

    public KingdomResourcesDto toDto()
    {
        return GsonFactory.createGson().fromJson(resourcesJson, KingdomResourcesDto.class);
    }

    public static KingdomResourcesEntity fromDto(KingdomResourcesDto dto)
    {
        String resourcesJson = GsonFactory.createGson().toJson(dto, KingdomResourcesDto.class);
        return new KingdomResourcesEntity(resourcesJson);
    }

    public KingdomResources toDomainModel()
    {
        var dto = toDto();
        return new KingdomResources(dto.getResources());
    }

    public static KingdomResourcesEntity fromDomainModel(KingdomResources resources)
    {
        var dto = new KingdomResourcesDto(resources.getResources());
        String resourcesJson = GsonFactory.createGson().toJson(dto, KingdomResourcesDto.class);
        return new KingdomResourcesEntity(resourcesJson);
    }
}
