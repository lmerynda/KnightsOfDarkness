package com.knightsofdarkness.web.utils;

import java.util.ArrayList;
import java.util.UUID;

import com.knightsofdarkness.web.Game;
import com.knightsofdarkness.web.common.kingdom.BuildingName;
import com.knightsofdarkness.web.common.kingdom.KingdomBuildingsDto;
import com.knightsofdarkness.web.common.kingdom.KingdomResourcesDto;
import com.knightsofdarkness.web.common.kingdom.KingdomTurnReport;
import com.knightsofdarkness.web.common.kingdom.KingdomUnitsDto;
import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.kingdom.UnitName;
import com.knightsofdarkness.web.common.kingdom.UnitsMapDto;
import com.knightsofdarkness.web.kingdom.model.KingdomBuildingsEntity;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;
import com.knightsofdarkness.web.kingdom.model.KingdomResourcesEntity;
import com.knightsofdarkness.web.kingdom.model.KingdomUnitsEntity;

public class KingdomBuilder
{
    private final KingdomResourcesDto resources;
    private final KingdomBuildingsDto buildings;
    private final KingdomUnitsDto units;
    private String name;

    public KingdomBuilder(Game game)
    {
        this.name = generateName();
        var startingConfiguration = game.getConfig().kingdomStartConfiguration();
        this.resources = new KingdomResourcesDto(startingConfiguration.resources().toMap());
        this.buildings = new KingdomBuildingsDto(startingConfiguration.buildings().toMap());
        this.units = new KingdomUnitsDto(startingConfiguration.units().toMap(), new UnitsMapDto().getUnits());
    }

    public static String generateName()
    {
        return "kingdom-" + UUID.randomUUID().toString().replace("-", "");
    }

    public KingdomBuilder withRichConfiguration()
    {
        for (var resource : ResourceName.values())
        {
            this.resources.setCount(resource, 500000);
        }
        this.resources.setCount(ResourceName.gold, 10000000);
        this.resources.setCount(ResourceName.unemployed, 10000);

        for (var building : BuildingName.values())
        {
            this.buildings.setCount(building, 200);
        }
        this.buildings.setCount(BuildingName.house, 4100);

        for (var unit : UnitName.values())
        {
            this.units.getAvailableUnits().put(unit, 1000);
        }

        return this;
    }

    public KingdomBuilder withResource(ResourceName resource, int count)
    {
        this.resources.setCount(resource, count);
        return this;
    }

    public KingdomBuilder withLand(int count)
    {
        this.resources.setCount(ResourceName.land, count);
        return this;
    }

    public KingdomBuilder withUnit(UnitName unit, int count)
    {
        this.units.getAvailableUnits().put(unit, count);
        return this;
    }

    public KingdomBuilder withBuilding(BuildingName building, int count)
    {
        this.buildings.setCount(building, count);
        return this;
    }

    public KingdomBuilder withName(String name)
    {
        this.name = name;
        return this;
    }

    public KingdomEntity build()
    {
        return new KingdomEntity(
                name,
                new KingdomResourcesEntity(resources.getResources()),
                new KingdomBuildingsEntity(buildings.getBuildings()),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new KingdomUnitsEntity(units.getAvailableUnits(), units.getMobileUnits()),
                new KingdomTurnReport());
    }
}
