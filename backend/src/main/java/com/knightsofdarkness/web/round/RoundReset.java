package com.knightsofdarkness.web.round;

import java.util.List;
import java.util.Set;

import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;
import com.knightsofdarkness.web.kingdom.model.KingdomSpecialBuildingAction;

/**
 * Every kingdom starts the new round from the starting configuration. Alliances stay. Bots keep the kind of special
 * buildings they specialize in, rebuilt from level 0.
 */
public final class RoundReset {
    private RoundReset()
    {
    }

    public static void reset(List<KingdomEntity> kingdoms, Set<String> botNames, GameConfig gameConfig)
    {
        for (var kingdom : kingdoms)
        {
            var specialBuildingTypes = kingdom.getSpecialBuildings().stream().map(building -> building.getBuildingType()).toList();
            kingdom.resetToStart(gameConfig);
            if (botNames.contains(kingdom.getName()))
            {
                var action = new KingdomSpecialBuildingAction(kingdom, gameConfig);
                specialBuildingTypes.forEach(action::startSpecialBuilding);
            }
        }
    }
}
