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
            case harvest -> news(gain(kingdom, report, ResourceName.food), "A bountiful harvest brought in {} more food");
            case goldVein -> news(gain(kingdom, report, ResourceName.gold), "Our miners struck a rich vein, {} more gold");
            case rats -> news(lose(kingdom, ResourceName.food), "Rats got into the granaries, {} food is lost");
            case fire -> news(lose(kingdom, ResourceName.tools), "A fire swept through the workshops and burned {} tools");
            case bandits -> {
                var guardsNeeded = (int) Math.ceil(kingdom.getResources().getCount(ResourceName.land) * config.events().guardsPerAcreAgainstBandits());
                if (kingdom.getUnits().getAvailableCount(UnitName.guard) >= guardsNeeded)
                {
                    yield Optional.of("Bandits came to our roads, but the guards drove them off");
                }
                var robbed = lose(kingdom, ResourceName.gold);
                yield robbed == 0 ? Optional.<String>empty() : Optional.of(Utils.format("Bandits robbed our roads of {} gold, {} guards would have kept them away", robbed, guardsNeeded));
            }
        };
        text.ifPresent(news -> report.event = news);
        return text;
    }

    /** fortune that moves nothing, a windfall on a kingdom that produced none or a loss from empty stores, is no news */
    private static Optional<String> news(int amount, String template)
    {
        return amount == 0 ? Optional.empty() : Optional.of(Utils.format(template, amount));
    }

    private int gain(KingdomEntity kingdom, KingdomTurnReport report, ResourceName resource)
    {
        var extra = (int) (report.resourcesProduced.getOrDefault(resource, 0) * config.events().windfallShare());
        kingdom.getResources().addCount(resource, extra);
        return extra;
    }

    private int lose(KingdomEntity kingdom, ResourceName resource)
    {
        var lost = (int) (kingdom.getResources().getCount(resource) * config.events().lossShare());
        kingdom.getResources().subtractCount(resource, lost);
        return lost;
    }
}
