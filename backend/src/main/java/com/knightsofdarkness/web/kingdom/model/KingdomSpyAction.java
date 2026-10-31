package com.knightsofdarkness.web.kingdom.model;

import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;

import com.knightsofdarkness.web.common.kingdom.BuildingName;
import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.kingdom.UnitName;
import com.knightsofdarkness.web.common.war.SendSpiesResult;
import com.knightsofdarkness.web.common.war.SpyReportDto;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.utils.Id;
import com.knightsofdarkness.web.utils.Utils;

/**
 * Spies sneak into the target right away. The mission succeeds when the spies outnumber the target counter intelligence
 * (its own spies and guards). Caught spies are lost, the survivors come back home.
 */
public class KingdomSpyAction {
    private final KingdomEntity kingdom;
    private final GameConfig gameConfig;

    public KingdomSpyAction(KingdomEntity kingdom, GameConfig gameConfig)
    {
        this.kingdom = kingdom;
        this.gameConfig = gameConfig;
    }

    public SendSpiesResult sendSpies(KingdomEntity target, int spies)
    {
        if (target.getName().equals(kingdom.getName()))
        {
            return SendSpiesResult.failure("You cannot spy on your own kingdom");
        }

        var availableSpies = kingdom.getUnits().getAvailableCount(UnitName.spy);
        if (spies <= 0)
        {
            return SendSpiesResult.failure("Need to send at least one spy");
        }
        if (spies > availableSpies)
        {
            return SendSpiesResult.failure(Utils.format("Not enough spies, available: {}, needed {}", availableSpies, spies));
        }

        var success = spies > getCounterIntelligence(target);
        if (success)
        {
            var report = new SpyReportDto(Id.generate(), Instant.now(), kingdom.getName(), target.getName(), true, spies, 0,
                    target.getResources().toEnumMap(), copyBuildings(target), new EnumMap<>(target.getUnits().getAvailableUnits()));
            return SendSpiesResult.success(Utils.format("Our spies returned from {} with intel", target.getName()), report);
        }

        var spiesLost = Math.max(1, (int) Math.ceil(spies * gameConfig.espionage().failedMissionLossRatio()));
        kingdom.getUnits().subtractAvailableCount(UnitName.spy, spiesLost);
        var report = new SpyReportDto(Id.generate(), Instant.now(), kingdom.getName(), target.getName(), false, spies, spiesLost,
                new EnumMap<>(ResourceName.class), new EnumMap<>(BuildingName.class), new EnumMap<>(UnitName.class));
        return SendSpiesResult.success(Utils.format("Our spies were caught in {}, {} did not return", target.getName(), spiesLost), report);
    }

    public double getCounterIntelligence(KingdomEntity target)
    {
        var espionage = gameConfig.espionage();
        var units = target.getUnits();
        return units.getAvailableCount(UnitName.spy) * espionage.counterIntelligencePerSpy() + units.getAvailableCount(UnitName.guard) * espionage.counterIntelligencePerGuard();
    }

    private static Map<BuildingName, Integer> copyBuildings(KingdomEntity target)
    {
        var buildings = new EnumMap<BuildingName, Integer>(BuildingName.class);
        for (var building : BuildingName.values())
        {
            buildings.put(building, target.getBuildings().getCount(building));
        }
        return buildings;
    }
}
