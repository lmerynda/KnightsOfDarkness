package com.knightsofdarkness.common.kingdom;

import java.util.EnumMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

public class KingdomResourcesDto {
    Map<ResourceName, Integer> resources = new EnumMap<>(ResourceName.class);

    public KingdomResourcesDto()
    {
        for (var name : ResourceName.values())
        {
            resources.put(name, 0);
        }
    }

    public int getCount(ResourceName name)
    {
        return resources.get(name);
    }

    public void setCount(ResourceName name, int count)
    {
        resources.put(name, count);
    }

    @SuppressWarnings("java:S107")
    public KingdomResourcesDto(int land, int buildingPoints, int unemployed, int gold, int iron, int food, int tools, int weapons, int turns)
    {
        resources.put(ResourceName.land, land);
        resources.put(ResourceName.buildingPoints, buildingPoints);
        resources.put(ResourceName.unemployed, unemployed);
        resources.put(ResourceName.gold, gold);
        resources.put(ResourceName.iron, iron);
        resources.put(ResourceName.food, food);
        resources.put(ResourceName.tools, tools);
        resources.put(ResourceName.weapons, weapons);
        resources.put(ResourceName.turns, turns);
    }

    @JsonAnyGetter
    public Map<ResourceName, Integer> getResources()
    {
        return resources;
    }

    @JsonAnySetter
    public void setResource(String key, int value)
    {
        resources.put(ResourceName.from(key), value);
    }

    @Override
    public String toString()
    {
        return "KingdomResourcesDto{" +
                "resources=" + resources +
                '}';
    }

    public int countAll()
    {
        return resources.values().stream().mapToInt(Integer::intValue).sum();
    }
}
