package com.knightsofdarkness.common.kingdom;

import java.util.Map;

public class KingdomUnitsDto {
    UnitsMapDto availableUnits = new UnitsMapDto();
    UnitsMapDto mobileUnits = new UnitsMapDto();

    public KingdomUnitsDto()
    {
    }

    public KingdomUnitsDto(Map<UnitName, Integer> availableUnits, Map<UnitName, Integer> mobileUnits)
    {
        this.availableUnits = new UnitsMapDto(availableUnits);
        this.mobileUnits = new UnitsMapDto(mobileUnits);
    }

    public KingdomUnitsDto(UnitsMapDto availableUnits, UnitsMapDto mobileUnits)
    {
        this.availableUnits = availableUnits;
        this.mobileUnits = mobileUnits;
    }

    public UnitsMapDto getAvailableUnits()
    {
        return availableUnits;
    }

    public UnitsMapDto getMobileUnits()
    {
        return mobileUnits;
    }

    // public int getCount(UnitName name)
    // {
    // return getAvailableCount(name);
    // }

    // public int getAvailableCount(UnitName name)
    // {
    // return availableUnits.get(name);
    // }

    // public void setCount(UnitName name, int count)
    // {
    // setAvailableCount(name, count);
    // }

    // public void setAvailableCount(UnitName name, int count)
    // {
    // availableUnits.put(name, count);
    // }

    // public int getMobileCount(UnitName name)
    // {
    // return mobileUnits.get(name);
    // }

    // public void setMobileCount(UnitName name, int count)
    // {
    // mobileUnits.put(name, count);
    // }

    // public int getTotalCount(UnitName name)
    // {
    // return getAvailableCount(name) + getMobileCount(name);
    // }

    // public int countAll()
    // {
    // return availableUnits.values().stream().mapToInt(Integer::intValue).sum()
    // + mobileUnits.values().stream().mapToInt(Integer::intValue).sum();
    // }

    // public Map<UnitName, Integer> getAvailableUnits()
    // {
    // return availableUnits;
    // }

    // public Map<UnitName, Integer> getMobileUnits()
    // {
    // return mobileUnits;
    // }

    // public void setAvailableUnits(Map<UnitName, Integer> availableUnits)
    // {
    // this.availableUnits = availableUnits;
    // }

    // public void setMobileUnits(Map<UnitName, Integer> mobileUnits)
    // {
    // this.mobileUnits = mobileUnits;
    // }

    @Override
    public String toString()
    {
        return "KingdomUnitsDto{" +
                "availableUnits=" + availableUnits +
                ", mobileUnits=" + mobileUnits +
                '}';
    }
}
