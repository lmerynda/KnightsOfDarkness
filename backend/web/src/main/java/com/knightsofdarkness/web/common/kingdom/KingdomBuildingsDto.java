package com.knightsofdarkness.web.common.kingdom;

import java.util.EnumMap;
import java.util.Map;

public class KingdomBuildingsDto {
    Map<BuildingName, Integer> buildings = new EnumMap<>(BuildingName.class);

    public KingdomBuildingsDto()
    {
        for (var name : BuildingName.values())
        {
            buildings.put(name, 0);
        }
    }

    public KingdomBuildingsDto(Map<BuildingName, Integer> buildings)
    {
        for (var name : BuildingName.values())
        {
            this.buildings.put(name, 0);
        }
        this.buildings.putAll(buildings);
    }

    @SuppressWarnings("java:S107")
    public KingdomBuildingsDto(int house, int goldMine, int ironMine, int workshop, int farm, int market, int barracks, int guardHouse, int spyGuild, int tower, int castle)
    {
        buildings.put(BuildingName.house, house);
        buildings.put(BuildingName.goldMine, goldMine);
        buildings.put(BuildingName.ironMine, ironMine);
        buildings.put(BuildingName.workshop, workshop);
        buildings.put(BuildingName.farm, farm);
        buildings.put(BuildingName.market, market);
        buildings.put(BuildingName.barracks, barracks);
        buildings.put(BuildingName.guardHouse, guardHouse);
        buildings.put(BuildingName.spyGuild, spyGuild);
        buildings.put(BuildingName.tower, tower);
        buildings.put(BuildingName.castle, castle);
    }

    public int getCount(BuildingName name)
    {
        return buildings.get(name);
    }

    public void setCount(BuildingName name, int count)
    {
        buildings.put(name, count);
    }

    public Map<BuildingName, Integer> getBuildings()
    {
        return buildings;
    }

    public void setBuilding(String key, int value)
    {
        buildings.put(BuildingName.from(key), value);
    }

    public String toString()
    {
        return buildings.toString();
    }

    public int countAll()
    {
        return buildings.values().stream().mapToInt(Integer::intValue).sum();
    }
}
