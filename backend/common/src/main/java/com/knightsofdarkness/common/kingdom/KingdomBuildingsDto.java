package com.knightsofdarkness.common.kingdom;

import java.util.EnumMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

public class KingdomBuildingsDto {
    Map<BuildingName, Integer> buildings = new EnumMap<>(BuildingName.class);

    public KingdomBuildingsDto()
    {
        for (var name : BuildingName.values())
        {
            buildings.put(name, 0);
        }
    }

    public int getCount(BuildingName name)
    {
        return buildings.get(name);
    }

    public void setCount(BuildingName name, int count)
    {
        buildings.put(name, count);
    }

    @SuppressWarnings("java:S107")
    public KingdomBuildingsDto(int houses, int goldMines, int ironMines, int workshops, int farms, int markets, int barracks, int guardHouses, int spyGuilds, int towers, int castles)
    {
        buildings.put(BuildingName.house, houses);
        buildings.put(BuildingName.goldMine, goldMines);
        buildings.put(BuildingName.ironMine, ironMines);
        buildings.put(BuildingName.workshop, workshops);
        buildings.put(BuildingName.farm, farms);
        buildings.put(BuildingName.market, markets);
        buildings.put(BuildingName.barracks, barracks);
        buildings.put(BuildingName.guardHouse, guardHouses);
        buildings.put(BuildingName.spyGuild, spyGuilds);
        buildings.put(BuildingName.tower, towers);
        buildings.put(BuildingName.castle, castles);
    }

    @JsonAnyGetter
    public Map<BuildingName, Integer> getBuildings()
    {
        return buildings;
    }

    @JsonAnySetter
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
