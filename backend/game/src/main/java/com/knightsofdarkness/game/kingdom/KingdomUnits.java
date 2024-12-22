package com.knightsofdarkness.game.kingdom;

import java.util.EnumMap;
import java.util.Map;

import com.knightsofdarkness.common.kingdom.UnitName;
import com.knightsofdarkness.common.kingdom.UnitsMapDto;

public class KingdomUnits {
    UnitsMapDto availableUnits = new UnitsMapDto();
    UnitsMapDto mobileUnits = new UnitsMapDto();

    public KingdomUnits()
    {
    }

    public KingdomUnits(KingdomUnits other)
    {
        availableUnits = new UnitsMapDto(other.availableUnits);
        mobileUnits = new UnitsMapDto(other.mobileUnits);
    }

    public KingdomUnits(Map<UnitName, Integer> availableUnits, Map<UnitName, Integer> mobileUnits)
    {
        this.availableUnits = new UnitsMapDto(availableUnits);
        this.mobileUnits = new UnitsMapDto(mobileUnits);
    }

    public KingdomUnits(UnitsMapDto availableUnits, UnitsMapDto mobileUnits)
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

    public int getAvailableCount(UnitName name)
    {
        return availableUnits.getCount(name);
    }

    public int getMobileCount(UnitName name)
    {
        return mobileUnits.getCount(name);
    }

    public int getTotalCount(UnitName name)
    {
        return availableUnits.getCount(name) + mobileUnits.getCount(name);
    }

    // TODO clean this method, this is only temporary to maintain API
    public void addCount(UnitName name, int count)
    {
        addAvailableCount(name, count);
    }

    public void addAvailableCount(UnitName name, int count)
    {
        availableUnits.setCount(name, availableUnits.getCount(name) + count);
    }

    public void addMobileCount(UnitName name, int count)
    {
        mobileUnits.setCount(name, mobileUnits.getCount(name) + count);
    }

    // TODO clean this method, this is only temporary to maintain API
    public void subtractCount(UnitName name, int count)
    {
        subtractAvailableCount(name, count);
    }

    public void subtractAvailableCount(UnitName name, int count)
    {
        availableUnits.setCount(name, availableUnits.getCount(name) - count);
    }

    public void subtractMobileCount(UnitName name, int count)
    {
        mobileUnits.setCount(name, mobileUnits.getCount(name) - count);
    }

    // TODO clean this method, this is only temporary to maintain API
    public void setCount(UnitName name, int count)
    {
        setAvailableCount(name, count);
    }

    public void setAvailableCount(UnitName name, int count)
    {
        availableUnits.setCount(name, count);
    }

    public void setMobileCount(UnitName name, int count)
    {
        mobileUnits.setCount(name, count);
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
        return availableUnits.countAll() + mobileUnits.countAll();
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
