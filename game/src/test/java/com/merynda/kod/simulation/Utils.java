package com.merynda.kod.simulation;

import com.merynda.kod.game.Game;
import com.merynda.kod.kingdom.BuildingName;
import com.merynda.kod.kingdom.ResourceName;
import com.merynda.kod.kingdom.UnitName;
import com.merynda.kod.utils.KingdomBuilder;

public class Utils {
    public static KingdomBuilder setupKingdomStartConfiguration(KingdomBuilder kingdomBuilder, Game game)
    {
        var startConfiguration = game.getConfig().kingdomStartConfiguration();
        for (var building : BuildingName.values())
        {
            kingdomBuilder.withBuilding(building, startConfiguration.buildings().getCount(building));
        }

        for (var resource : ResourceName.values())
        {
            kingdomBuilder.withResource(resource, startConfiguration.resources().getCount(resource));
        }

        for (var unit : UnitName.values())
        {
            kingdomBuilder.withUnit(unit, startConfiguration.units().getCount(unit));
        }

        return kingdomBuilder;
    }
}
