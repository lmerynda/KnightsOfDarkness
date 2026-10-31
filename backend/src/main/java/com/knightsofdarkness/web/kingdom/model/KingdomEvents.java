package com.knightsofdarkness.web.kingdom.model;

import java.util.Optional;

import com.knightsofdarkness.web.common.kingdom.KingdomTurnReport;
import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.kingdom.UnitName;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.utils.Utils;

/**
 * What fortune brings after a turn has passed. The caller rolls the dice, so the outcome of a given roll is fixed, and
 * the forecast of the next turn never includes fortune.
 */
public class KingdomEvents
{
    private final GameConfig config;

    public KingdomEvents(GameConfig config)
    {
        this.config = config;
    }

    /**
     * @param roll
     *            a number in [0, 1), an event happens when it falls below the chance per turn
     * @param event
     *            which event happens if one does
     */
    public Optional<String> happen(KingdomEntity kingdom, KingdomTurnReport report, double roll, KingdomEvent event)
    {
        if (roll >= config.events().chancePerTurn())
        {
            return Optional.empty();
        }

        var text = switch (event)
        {
            case harvest -> {
                var extra = windfall(report, ResourceName.food);
                kingdom.getResources().addCount(ResourceName.food, extra);
                yield Utils.format("A bountiful harvest brought in {} more food", extra);
            }
            case goldVein -> {
                var extra = windfall(report, ResourceName.gold);
                kingdom.getResources().addCount(ResourceName.gold, extra);
                yield Utils.format("Our miners struck a rich vein, {} more gold", extra);
            }
            case rats -> Utils.format("Rats got into the granaries, {} food is lost", lose(kingdom, ResourceName.food));
            case fire -> Utils.format("A fire swept through the workshops and burned {} tools", lose(kingdom, ResourceName.tools));
            case bandits -> {
                var guardsNeeded = (int) Math.ceil(kingdom.getResources().getCount(ResourceName.land) * config.events().guardsPerAcreAgainstBandits());
                if (kingdom.getUnits().getAvailableCount(UnitName.guard) >= guardsNeeded)
                {
                    yield "Bandits came to our roads, but the guards drove them off";
                }
                yield Utils.format("Bandits robbed our roads of {} gold, {} guards would have kept them away", lose(kingdom, ResourceName.gold), guardsNeeded);
            }
        };
        report.event = text;
        return Optional.of(text);
    }

    private int windfall(KingdomTurnReport report, ResourceName resource)
    {
        return (int) (report.resourcesProduced.getOrDefault(resource, 0) * config.events().windfallShare());
    }

    private int lose(KingdomEntity kingdom, ResourceName resource)
    {
        var lost = (int) (kingdom.getResources().getCount(resource) * config.events().lossShare());
        kingdom.getResources().subtractCount(resource, lost);
        return lost;
    }
}
