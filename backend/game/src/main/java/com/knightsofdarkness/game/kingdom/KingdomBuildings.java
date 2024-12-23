package com.knightsofdarkness.game.kingdom;

import java.util.EnumMap;
import java.util.Map;

import com.knightsofdarkness.common.kingdom.BuildingName;

public class KingdomBuildings {
    Map<BuildingName, Integer> buildings = new EnumMap<>(BuildingName.class);

    public KingdomBuildings()
    {
        for (var name : BuildingName.values())
        {
            buildings.put(name, 0);
        }
    }

    public KingdomBuildings(KingdomBuildings other)
    {
        this.buildings = other.buildings;
    }

    public KingdomBuildings(Map<BuildingName, Integer> buildings)
    {
        this.buildings = buildings;
    }

    public int countAll()
    {
        return buildings.values().stream().mapToInt(Integer::intValue).sum();
    }

    public int getCount(BuildingName name)
    {
        return buildings.get(name);
    }

    public int getCapacity(BuildingName name, int buildingCapacity)
    {
        return getCount(name) * buildingCapacity;
    }

    public void addCount(BuildingName name, int count)
    {
        buildings.put(name, buildings.get(name) + count);
    }

    public void subtractCount(BuildingName name, int count)
    {
        assert buildings.get(name) >= count;
        buildings.put(name, buildings.get(name) - count);
    }

    public void setCount(BuildingName name, int count)
    {
        buildings.put(name, count);
    }

    public Map<BuildingName, Integer> getBuildings()
    {
        return buildings;
    }
}
