package com.knightsofdarkness.web.kingdom.model;

import java.util.EnumMap;
import java.util.Map;

import com.knightsofdarkness.common.kingdom.BuildingName;
import com.knightsofdarkness.common.kingdom.KingdomBuildingsDto;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Transient;

@Embeddable
@Access(AccessType.FIELD)
public class KingdomBuildingsEntity {
    int house;
    int goldMine;
    int ironMine;
    int workshop;
    int farm;
    int market;
    int barracks;
    int guardHouse;
    int spyGuild;
    int tower;
    int castle;

    @Transient
    EnumMap<BuildingName, Integer> buildings = new EnumMap<>(BuildingName.class);

    public KingdomBuildingsEntity()
    {
    }

    public KingdomBuildingsEntity(Map<BuildingName, Integer> buildingsMap)
    {
        buildings.putAll(buildingsMap);
        loadMap(buildings);
    }

    public KingdomBuildingsDto toDto()
    {
        return new KingdomBuildingsDto(buildings);
    }

    public void loadMap(Map<BuildingName, Integer> buildingsMap)
    {
        house = buildingsMap.get(BuildingName.house);
        goldMine = buildingsMap.get(BuildingName.goldMine);
        ironMine = buildingsMap.get(BuildingName.ironMine);
        workshop = buildingsMap.get(BuildingName.workshop);
        farm = buildingsMap.get(BuildingName.farm);
        market = buildingsMap.get(BuildingName.market);
        barracks = buildingsMap.get(BuildingName.barracks);
        guardHouse = buildingsMap.get(BuildingName.guardHouse);
        spyGuild = buildingsMap.get(BuildingName.spyGuild);
        tower = buildingsMap.get(BuildingName.tower);
        castle = buildingsMap.get(BuildingName.castle);
    }

    public EnumMap<BuildingName, Integer> toEnumMap()
    {
        var map = new EnumMap<BuildingName, Integer>(BuildingName.class);
        map.put(BuildingName.house, house);
        map.put(BuildingName.goldMine, goldMine);
        map.put(BuildingName.ironMine, ironMine);
        map.put(BuildingName.workshop, workshop);
        map.put(BuildingName.farm, farm);
        map.put(BuildingName.market, market);
        map.put(BuildingName.barracks, barracks);
        map.put(BuildingName.guardHouse, guardHouse);
        map.put(BuildingName.spyGuild, spyGuild);
        map.put(BuildingName.tower, tower);
        map.put(BuildingName.castle, castle);
        return map;
    }

    public void syncBuildings()
    {
        loadMap(buildings);
    }

    @PostLoad
    public void loadBuildings()
    {
        buildings = toEnumMap();
    }

    public int getCapacity(BuildingName name, int buildingCapacity)
    {
        return buildings.get(name) * buildingCapacity;
    }

    public int getCount(BuildingName name)
    {
        return buildings.get(name);
    }

    public void addCount(BuildingName name, int count)
    {
        buildings.put(name, buildings.get(name) + count);
    }

    public void subtractCount(BuildingName buildingName, int howManyToDemolish)
    {
        buildings.put(buildingName, buildings.get(buildingName) - howManyToDemolish);
    }

    // TODO is it really needed?
    public void setCount(BuildingName name, int count)
    {
        buildings.put(name, count);
    }

    public int countAll()
    {
        return buildings.values().stream().mapToInt(Integer::intValue).sum();
    }
}
