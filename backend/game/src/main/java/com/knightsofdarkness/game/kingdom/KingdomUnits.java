package com.knightsofdarkness.game.kingdom;

import java.util.EnumMap;
import java.util.Map;

import com.knightsofdarkness.common.kingdom.UnitName;

public class KingdomUnits {
    Map<UnitName, Integer> availableUnits = new EnumMap<>(UnitName.class);
    Map<UnitName, Integer> mobileUnits = new EnumMap<>(UnitName.class);

    public KingdomUnits()
    {
        for (var name : UnitName.values())
        {
            availableUnits.put(name, 0);
            mobileUnits.put(name, 0);
        }
    }

    public KingdomUnits(KingdomUnits other)
    {
        availableUnits = new EnumMap<>(other.availableUnits);
        mobileUnits = new EnumMap<>(other.mobileUnits);
    }

    public KingdomUnits(Map<UnitName, Integer> availableUnits, Map<UnitName, Integer> mobileUnits)
    {
        this.availableUnits = availableUnits;
        this.mobileUnits = mobileUnits;
    }

    public Map<UnitName, Integer> getAvailableUnits()
    {
        return availableUnits;
    }

    public Map<UnitName, Integer> getMobileUnits()
    {
        return mobileUnits;
    }

    public int getAvailableCount(UnitName name)
    {
        return availableUnits.get(name);
    }

    public int getMobileCount(UnitName name)
    {
        return mobileUnits.get(name);
    }

    public int getTotalCount(UnitName name)
    {
        return availableUnits.get(name) + mobileUnits.get(name);
    }

    // TODO clean this method, this is only temporary to maintain API
    public void addCount(UnitName name, int count)
    {
        addAvailableCount(name, count);
    }

    public void addAvailableCount(UnitName name, int count)
    {
        availableUnits.put(name, availableUnits.get(name) + count);
    }

    public void addMobileCount(UnitName name, int count)
    {
        mobileUnits.put(name, mobileUnits.get(name) + count);
    }

    // TODO clean this method, this is only temporary to maintain API
    public void subtractCount(UnitName name, int count)
    {
        subtractAvailableCount(name, count);
    }

    public void subtractAvailableCount(UnitName name, int count)
    {
        availableUnits.put(name, availableUnits.get(name) - count);
    }

    public void subtractMobileCount(UnitName name, int count)
    {
        mobileUnits.put(name, mobileUnits.get(name) - count);
    }

    // TODO clean this method, this is only temporary to maintain API
    public void setCount(UnitName name, int count)
    {
        setAvailableCount(name, count);
    }

    public void setAvailableCount(UnitName name, int count)
    {
        availableUnits.put(name, count);
    }

    public void setMobileCount(UnitName name, int count)
    {
        mobileUnits.put(name, count);
    }

    public void moveAvailableToMobile(UnitName name, int count)
    {
        subtractAvailableCount(name, count);
        addMobileCount(name, count);
    }

    public void moveMobileToAvailable(UnitName name, int count)
    {
        subtractMobileCount(name, count);
        addAvailableCount(name, count);
    }

    public int countAll()
    {
        return availableUnits.values().stream().mapToInt(Integer::intValue).sum() +
                mobileUnits.values().stream().mapToInt(Integer::intValue).sum();
    }

    public Map<UnitName, Double> getUnitsRatios()
    {
        var totalPopulation = countAll();
        var unitRatios = new EnumMap<UnitName, Double>(UnitName.class);
        for (var unit : UnitName.values())
        {
            unitRatios.put(unit, (double) getTotalCount(unit) / totalPopulation);
        }
        return unitRatios;
    }

    @Override
    public String toString()
    {
        return "Available Units: " + availableUnits.toString() + ", Mobile Units: " + mobileUnits.toString();
    }
}
