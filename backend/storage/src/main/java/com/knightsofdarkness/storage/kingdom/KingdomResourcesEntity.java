package com.knightsofdarkness.storage.kingdom;

import java.util.EnumMap;
import java.util.Map;

import com.knightsofdarkness.common.kingdom.KingdomResourcesDto;
import com.knightsofdarkness.common.kingdom.ResourceName;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Transient;

@Embeddable
@Access(AccessType.FIELD)
public class KingdomResourcesEntity {
    int land;
    int buildingPoints;
    int unemployed;
    int gold;
    int iron;
    int food;
    int tools;
    int weapons;
    int turns;

    @Transient
    EnumMap<ResourceName, Integer> resources = new EnumMap<>(ResourceName.class);

    public KingdomResourcesEntity()
    {
    }

    public KingdomResourcesDto toDto()
    {
        return new KingdomResourcesDto(toEnumMap());
    }

    public void loadMap(Map<ResourceName, Integer> resourceMap)
    {
        this.land = resourceMap.get(ResourceName.land);
        this.buildingPoints = resourceMap.get(ResourceName.buildingPoints);
        this.unemployed = resourceMap.get(ResourceName.unemployed);
        this.gold = resourceMap.get(ResourceName.gold);
        this.iron = resourceMap.get(ResourceName.iron);
        this.food = resourceMap.get(ResourceName.food);
        this.tools = resourceMap.get(ResourceName.tools);
        this.weapons = resourceMap.get(ResourceName.weapons);
        this.turns = resourceMap.get(ResourceName.turns);
    }

    public EnumMap<ResourceName, Integer> toEnumMap()
    {
        var map = new EnumMap<ResourceName, Integer>(ResourceName.class);
        map.put(ResourceName.land, land);
        map.put(ResourceName.buildingPoints, buildingPoints);
        map.put(ResourceName.unemployed, unemployed);
        map.put(ResourceName.gold, gold);
        map.put(ResourceName.iron, iron);
        map.put(ResourceName.food, food);
        map.put(ResourceName.tools, tools);
        map.put(ResourceName.weapons, weapons);
        map.put(ResourceName.turns, turns);
        return map;
    }

    @PrePersist
    @PreUpdate
    public void syncResources()
    {
        loadMap(resources);
    }

    @PostLoad
    public void loadResources()
    {
        resources = toEnumMap();
    }
}
