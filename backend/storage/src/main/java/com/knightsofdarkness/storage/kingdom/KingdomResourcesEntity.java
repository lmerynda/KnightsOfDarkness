package com.knightsofdarkness.storage.kingdom;

import java.util.EnumMap;

import com.knightsofdarkness.common.kingdom.KingdomResourcesDto;
import com.knightsofdarkness.common.kingdom.ResourceName;
import com.knightsofdarkness.game.kingdom.KingdomResources;

import jakarta.persistence.Embeddable;

@Embeddable
public class KingdomResourcesEntity {
    EnumMap<ResourceName, Integer> resources = new EnumMap<>(ResourceName.class);

    public KingdomResourcesEntity()
    {
    }

    public KingdomResourcesEntity(KingdomResources resources)
    {
        // TODO rework remove indirection
        this.resources = new EnumMap<>(resources.getResources());
    }

    public KingdomResourcesEntity(KingdomResourcesDto dto)
    {
        // TODO rework remove indirection, and if possible this constructor
        resources = new EnumMap<>(dto.getResources());
    }

    public KingdomResourcesDto toDto()
    {
        return new KingdomResourcesDto(resources);
    }
}
