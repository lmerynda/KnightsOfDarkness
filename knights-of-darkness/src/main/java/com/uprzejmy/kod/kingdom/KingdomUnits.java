package com.uprzejmy.kod.kingdom;

import java.util.EnumMap;
import java.util.Map;

public class KingdomUnits
{
    Map<UnitName, Integer> units = new EnumMap<>(UnitName.class);

    public KingdomUnits()
    {
        for (var name : UnitName.values())
        {
            units.put(name, 0);
        }
    }

    public KingdomUnits(KingdomUnits other)
    {
        units = new EnumMap<>(other.units);
    }

    public int getCount(UnitName name)
    {
        return units.get(name);
    }

    public void addCount(UnitName name, int count)
    {
        units.put(name, units.get(name) + count);
    }

    public void subtractCount(UnitName name, int count)
    {
        units.put(name, units.get(name) - count);
    }

    public void setCount(UnitName name, int count)
    {
        units.put(name, count);
    }

    public int countAll()
    {
        return units.values().stream().mapToInt(Integer::intValue).sum();
    }

    public String toString()
    {
        return units.toString();
    }
}
