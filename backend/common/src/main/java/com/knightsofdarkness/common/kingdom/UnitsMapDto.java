package com.knightsofdarkness.common.kingdom;

import java.util.EnumMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;

public class UnitsMapDto {
    private Map<UnitName, Integer> units = new EnumMap<>(UnitName.class);

    public UnitsMapDto()
    {
        for (var name : UnitName.values())
        {
            units.put(name, 0);
        }
    }

    public UnitsMapDto(Map<UnitName, Integer> units)
    {
        this.units.putAll(units);
    }

    public int getCount(UnitName name)
    {
        return units.get(name);
    }

    public void setCount(UnitName name, int count)
    {
        units.put(name, count);
    }

    @JsonAnySetter
    public void setUnit(String name, int count)
    {
        try
        {
            UnitName unitName = UnitName.valueOf(name);
            units.put(unitName, count);
        } catch (IllegalArgumentException e)
        {
            // do nothing for now
        }
    }

    @Override
    public String toString()
    {
        return "UnitsMapDto{" +
                "units=" + units +
                '}';
    }
}
