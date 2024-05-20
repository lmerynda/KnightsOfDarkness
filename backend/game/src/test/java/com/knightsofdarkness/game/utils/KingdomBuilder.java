package com.knightsofdarkness.game.utils;

import com.knightsofdarkness.game.game.Game;
import com.knightsofdarkness.game.kingdom.BuildingName;
import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.game.kingdom.KingdomBuildings;
import com.knightsofdarkness.game.kingdom.KingdomResources;
import com.knightsofdarkness.game.kingdom.KingdomUnits;
import com.knightsofdarkness.game.kingdom.ResourceName;
import com.knightsofdarkness.game.kingdom.UnitName;

public class KingdomBuilder {
    private final KingdomResources resources;
    private final KingdomBuildings buildings;
    private final KingdomUnits units;
    private final Game game;
    private String name;

    public KingdomBuilder(Game game)
    {
        this.name = "test-kingdom";
        this.resources = new KingdomResources();
        for (var resource : ResourceName.values())
        {
            this.resources.setCount(resource, 500000);
        }
        this.resources.setCount(ResourceName.gold, 10000000);

        this.buildings = new KingdomBuildings();
        for (var building : BuildingName.values())
        {
            this.buildings.setCount(building, 1000);
        }
        this.buildings.setCount(BuildingName.house, 3000);

        this.units = new KingdomUnits();
        for (var unit : UnitName.values())
        {
            this.units.setCount(unit, 1000);
        }

        this.game = game;
    }

    public KingdomBuilder withResource(ResourceName resource, int count)
    {
        this.resources.setCount(resource, count);
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
        return new Kingdom(name, game.getConfig(), game.getMarket(), new KingdomResources(resources), new KingdomBuildings(buildings), new KingdomUnits(units));
    }
}
