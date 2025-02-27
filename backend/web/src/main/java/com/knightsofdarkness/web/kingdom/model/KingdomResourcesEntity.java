package com.knightsofdarkness.web.kingdom.model;

import java.util.EnumMap;
import java.util.Map;

import com.knightsofdarkness.common.kingdom.KingdomResourcesDto;
import com.knightsofdarkness.common.kingdom.ResourceName;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.PostLoad;
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

    public KingdomResourcesEntity(Map<ResourceName, Integer> resourceMap)
    {
        resources.putAll(resourceMap);
        loadMap(resources);
    }

    public KingdomResourcesDto toDto()
    {
        return new KingdomResourcesDto(toEnumMap());
    }

    public void loadMap(Map<ResourceName, Integer> resourceMap)
    {
        land = resourceMap.get(ResourceName.land);
        buildingPoints = resourceMap.get(ResourceName.buildingPoints);
        unemployed = resourceMap.get(ResourceName.unemployed);
        gold = resourceMap.get(ResourceName.gold);
        iron = resourceMap.get(ResourceName.iron);
        food = resourceMap.get(ResourceName.food);
        tools = resourceMap.get(ResourceName.tools);
        weapons = resourceMap.get(ResourceName.weapons);
        turns = resourceMap.get(ResourceName.turns);
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
