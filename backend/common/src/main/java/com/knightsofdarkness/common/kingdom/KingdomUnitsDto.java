package com.knightsofdarkness.common.kingdom;

import java.util.EnumMap;
import java.util.Map;

public class KingdomUnitsDto {
    Map<UnitName, Integer> availableUnits = new EnumMap<>(UnitName.class);
    Map<UnitName, Integer> mobileUnits = new EnumMap<>(UnitName.class);

    public KingdomUnitsDto()
    {
        for (var name : UnitName.values())
        {
            availableUnits.put(name, 0);
            mobileUnits.put(name, 0);
        }
    }

    public KingdomUnitsDto(Map<UnitName, Integer> availableUnits, Map<UnitName, Integer> mobileUnits)
    {
        for (var name : UnitName.values())
        {
            this.availableUnits.put(name, 0);
            this.mobileUnits.put(name, 0);
        }
        this.availableUnits.putAll(availableUnits);
        this.mobileUnits.putAll(mobileUnits);
    }

    public int getCount(UnitName name)
    {
        return getAvailableCount(name);
    }

    public int getAvailableCount(UnitName name)
    {
        return availableUnits.get(name);
    }

    public void setCount(UnitName name, int count)
    {
        setAvailableCount(name, count);
    }

    public void setAvailableCount(UnitName name, int count)
    {
        availableUnits.put(name, count);
    }

    public int getMobileCount(UnitName name)
    {
        return mobileUnits.get(name);
    }

    public void setMobileCount(UnitName name, int count)
    {
        mobileUnits.put(name, count);
    }

    public int getTotalCount(UnitName name)
    {
        return getAvailableCount(name) + getMobileCount(name);
    }

    public int countAll()
    {
        return availableUnits.values().stream().mapToInt(Integer::intValue).sum()
                + mobileUnits.values().stream().mapToInt(Integer::intValue).sum();
    }

    public Map<UnitName, Integer> getAvailableUnits()
    {
        return availableUnits;
    }

    public Map<UnitName, Integer> getMobileUnits()
    {
        return mobileUnits;
    }

    public void setAvailableUnits(Map<UnitName, Integer> availableUnits)
    {
        this.availableUnits = availableUnits;
    }

    public void setMobileUnits(Map<UnitName, Integer> mobileUnits)
    {
        this.mobileUnits = mobileUnits;
    }

    @Override
    public String toString()
    {
        return "KingdomUnitsDto{" +
                "availableUnits=" + availableUnits +
                ", mobileUnits=" + mobileUnits +
                '}';
    }
}
