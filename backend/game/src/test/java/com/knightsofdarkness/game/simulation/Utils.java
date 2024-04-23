package com.knightsofdarkness.game.simulation;

import com.knightsofdarkness.game.game.Game;
import com.knightsofdarkness.game.kingdom.BuildingName;
import com.knightsofdarkness.game.kingdom.ResourceName;
import com.knightsofdarkness.game.kingdom.UnitName;
import com.knightsofdarkness.game.utils.KingdomBuilder;

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
