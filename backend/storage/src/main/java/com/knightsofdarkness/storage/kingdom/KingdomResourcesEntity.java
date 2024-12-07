package com.knightsofdarkness.storage.kingdom;

import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.knightsofdarkness.common.kingdom.KingdomResourcesDto;
import com.knightsofdarkness.common.kingdom.ResourceName;
import com.knightsofdarkness.game.kingdom.KingdomResources;

import jakarta.persistence.Embeddable;

@Embeddable
class KingdomResourcesEntity {
    @JdbcTypeCode(SqlTypes.JSON)
    public Map<ResourceName, Integer> resources;

    public KingdomResourcesEntity()
    {
    }

    public KingdomResourcesEntity(Map<ResourceName, Integer> resources)
    {
        this.resources = resources;
    }

    public KingdomResourcesDto toDto()
    {
        return new KingdomResourcesDto(resources);
    }

    public static KingdomResourcesEntity fromDto(KingdomResourcesDto dto)
    {
        return new KingdomResourcesEntity(dto.getResources());
    }

    public KingdomResources toDomainModel()
    {
        return new KingdomResources(resources);
    }

    public static KingdomResourcesEntity fromDomainModel(KingdomResources resources)
    {
        return new KingdomResourcesEntity(resources.getResources());
    }
}
