package com.knightsofdarkness.web.simulation;

import com.knightsofdarkness.web.Game;
import com.knightsofdarkness.web.common.kingdom.BuildingName;
import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.kingdom.UnitName;
import com.knightsofdarkness.web.utils.KingdomBuilder;

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
