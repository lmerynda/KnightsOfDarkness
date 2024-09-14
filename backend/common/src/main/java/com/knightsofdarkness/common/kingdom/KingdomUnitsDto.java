package com.knightsofdarkness.common.kingdom;

import java.util.EnumMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

public class KingdomUnitsDto {
    Map<UnitName, Integer> units = new EnumMap<>(UnitName.class);

    public KingdomUnitsDto()
    {
        for (var name : UnitName.values())
        {
            units.put(name, 0);
        }
    }

    public int getCount(UnitName name)
    {
        return units.get(name);
    }

    public void setCount(UnitName name, int count)
    {
        units.put(name, count);
    }

    @SuppressWarnings("java:S107")
    public KingdomUnitsDto(int goldMiners, int ironMiners, int farmers, int blacksmiths, int builders, int carriers, int guards, int spies, int infantry, int bowmen, int cavalry)
    {
        units.put(UnitName.goldMiner, goldMiners);
        units.put(UnitName.ironMiner, ironMiners);
        units.put(UnitName.farmer, farmers);
        units.put(UnitName.blacksmith, blacksmiths);
        units.put(UnitName.builder, builders);
        units.put(UnitName.carrier, carriers);
        units.put(UnitName.guard, guards);
        units.put(UnitName.spy, spies);
        units.put(UnitName.infantry, infantry);
        units.put(UnitName.bowmen, bowmen);
        units.put(UnitName.cavalry, cavalry);
    }

    @JsonAnyGetter
    public Map<UnitName, Integer> getUnits()
    {
        return units;
    }

    @JsonAnySetter
    public void setResource(String key, int value)
    {
        units.put(UnitName.from(key), value);
    }

    public String toString()
    {
        return units.toString();
    }

    public int countAll()
    {
        return units.values().stream().mapToInt(Integer::intValue).sum();
    }
}
