package com.knightsofdarkness.game.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
    private Long id;

    public KingdomBuilder(Game game)
    {
        this.name = "test-kingdom";
        // TODO assign to id a number generated from this.name which is guaranteed to be unique
        this.id = generateId(this.name);
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

    private Long generateId(String name)
    {
        try
        {
            // Get SHA-256 hash of the name
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] messageDigest = md.digest(name.getBytes());

            // Convert byte array to BigInteger
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert BigInteger to long
            long uniqueId = no.longValue();

            // Ensure non-negative unique ID
            if (uniqueId < 0)
            {
                uniqueId = -uniqueId;
            }

            return uniqueId;
        } catch (NoSuchAlgorithmException e)
        {
            // Handle exception
            e.printStackTrace();
            return 0L;
        }
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
        this.id = generateId(name);
        return this;
    }

    public Kingdom build()
    {
        return new Kingdom(id, name, game, new KingdomResources(resources), new KingdomBuildings(buildings), new KingdomUnits(units));
    }
}
