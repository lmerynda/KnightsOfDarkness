package com.knightsofdarkness.web.common.kingdom;

import java.util.EnumMap;
import java.util.Map;

public class UnitsMapDto {
    Map<UnitName, Integer> units = new EnumMap<>(UnitName.class);

    public UnitsMapDto()
    {
        for (var name : UnitName.values())
        {
            units.put(name, 0);
        }
    }

    public UnitsMapDto(Map<UnitName, Integer> units)
    {
        for (var name : UnitName.values())
        {
            this.units.put(name, 0);
        }

        this.units.putAll(units);
    }

    public UnitsMapDto(UnitsMapDto other)
    {
        this.units.putAll(other.units);
    }

    public int getCount(UnitName name)
    {
        return units.get(name);
    }

    public void setCount(UnitName name, int count)
    {
        units.put(name, count);
    }

    public void subtractCount(UnitName name, int count)
    {
        units.put(name, units.get(name) - count);
    }

    public Map<UnitName, Integer> getUnits()
    {
        return units;
    }

    public int countAll()
    {
        return units.values().stream().mapToInt(Integer::intValue).sum();
    }

    public int countMilitary()
    {
        return UnitName.getMilitaryUnits().stream().mapToInt(units::get).sum();
    }

    public Map<UnitName, Double> getMilitaryUnitsRatios()
    {
        var totalMilitaryCount = countMilitary();
        var unitRatios = new EnumMap<UnitName, Double>(UnitName.class);
        for (var unit : UnitName.getMilitaryUnits())
        {
            var count = units.get(unit);
            unitRatios.put(unit, (double) count / totalMilitaryCount);
        }

        return unitRatios;
    }

    public void setUnit(String key, int value)
    {
        units.put(UnitName.from(key), value);
    }

    @Override
    public String toString()
    {
        return units.toString();
    }
}
