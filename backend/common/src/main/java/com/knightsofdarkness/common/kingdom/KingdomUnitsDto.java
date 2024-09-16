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
    public KingdomUnitsDto(int goldMiner, int ironMiner, int farmer, int blacksmith, int builder, int carrier, int guard, int spy, int infantry, int bowman, int cavalry)
    {
        units.put(UnitName.goldMiner, goldMiner);
        units.put(UnitName.ironMiner, ironMiner);
        units.put(UnitName.farmer, farmer);
        units.put(UnitName.blacksmith, blacksmith);
        units.put(UnitName.builder, builder);
        units.put(UnitName.carrier, carrier);
        units.put(UnitName.guard, guard);
        units.put(UnitName.spy, spy);
        units.put(UnitName.infantry, infantry);
        units.put(UnitName.bowman, bowman);
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
