package com.knightsofdarkness.game.utils;

import com.knightsofdarkness.common.kingdom.BuildingName;
import com.knightsofdarkness.common.kingdom.ResourceName;
import com.knightsofdarkness.common.kingdom.UnitName;
import com.knightsofdarkness.game.Game;
import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.game.kingdom.KingdomBuildings;
import com.knightsofdarkness.game.kingdom.KingdomResources;
import com.knightsofdarkness.game.kingdom.KingdomUnits;

public class KingdomBuilder {
    private final KingdomResources resources;
    private final KingdomBuildings buildings;
    private final KingdomUnits units;
    private final Game game;
    private String name;

    public KingdomBuilder(Game game)
    {
        this.name = "test-kingdom";
        var startingConfiguration = game.getConfig().kingdomStartConfiguration();
        this.resources = new KingdomResources();
        for (var resource : ResourceName.values())
        {
            this.resources.setCount(resource, startingConfiguration.resources().getCount(resource));
        }

        this.buildings = new KingdomBuildings();
        for (var building : BuildingName.values())
        {
            this.buildings.setCount(building, startingConfiguration.buildings().getCount(building));
        }

        this.units = new KingdomUnits();
        for (var unit : UnitName.values())
        {
            this.units.setCount(unit, startingConfiguration.units().getCount(unit));
        }

        this.game = game;
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
            this.units.setCount(unit, 1000);
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
        this.units.setCount(unit, count);
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

    public Kingdom build()
    {
        // TODO fix builder to return kingdom again
        return new Kingdom(name, null, resources, buildings, null, null, null, units, null);
        // return new Kingdom(name, game.getConfig(), new KingdomResources(resources), new KingdomBuildings(buildings), new ArrayList<KingdomSpecialBuilding>(), new ArrayList<KingdomCarriersOnTheMove>(), new
        // ArrayList<KingdomOngoingAttack>(),
        // new KingdomUnits(units), new KingdomTurnReport());
    }
}
