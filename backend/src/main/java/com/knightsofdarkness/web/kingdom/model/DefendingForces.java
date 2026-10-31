package com.knightsofdarkness.web.kingdom.model;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.knightsofdarkness.web.common.kingdom.UnitName;
import com.knightsofdarkness.web.common.kingdom.UnitsMapDto;

/**
 * Everyone who defends a kingdom in battle: its own soldiers at home and the allied garrisons standing with it, fighting
 * as one army. Losses of each unit type are shared in proportion to how many of that type each side brought.
 */
final class DefendingForces
{
    private final KingdomEntity host;
    private final List<KingdomReinforcementEntity> garrisons;

    DefendingForces(KingdomEntity host)
    {
        this.host = host;
        this.garrisons = host.getStandingGarrisons();
    }

    int count(UnitName unit)
    {
        return host.getUnits().getAvailableCount(unit) + garrisons.stream().mapToInt(garrison -> garrison.getUnits().getCount(unit)).sum();
    }

    Map<UnitName, Double> militaryRatios()
    {
        var total = UnitName.getMilitaryUnits().stream().mapToInt(this::count).sum();
        var ratios = new EnumMap<UnitName, Double>(UnitName.class);
        for (var unit : UnitName.getMilitaryUnits())
        {
            ratios.put(unit, total == 0 ? 0.0 : (double) count(unit) / total);
        }
        return ratios;
    }

    /**
     * every side loses its share rounded down, and whatever rounding leaves falls on the sides that still have the most
     */
    void lose(UnitName unit, int count)
    {
        var total = count(unit);
        if (count <= 0 || total <= 0)
        {
            return;
        }

        var sides = garrisons.size() + 1;
        var have = new int[sides];
        var lost = new int[sides];
        have[0] = host.getUnits().getAvailableCount(unit);
        for (int side = 1; side < sides; side++)
        {
            have[side] = garrisons.get(side - 1).getUnits().getCount(unit);
        }
        var toLose = Math.min(count, total);
        var assigned = 0;
        for (int side = 0; side < sides; side++)
        {
            lost[side] = (int) ((long) toLose * have[side] / total);
            assigned += lost[side];
        }
        while (assigned < toLose)
        {
            var most = 0;
            for (int side = 1; side < sides; side++)
            {
                if (have[side] - lost[side] > have[most] - lost[most])
                {
                    most = side;
                }
            }
            lost[most]++;
            assigned++;
        }

        host.getUnits().subtractAvailableCount(unit, lost[0]);
        for (int side = 1; side < sides; side++)
        {
            garrisons.get(side - 1).lose(unit, lost[side]);
        }
    }

    /**
     * the whole defending army, soldiers and the host's guards
     */
    UnitsMapDto snapshot()
    {
        var units = new UnitsMapDto();
        for (var unit : UnitName.getMilitaryUnits())
        {
            units.setCount(unit, count(unit));
        }
        units.setCount(UnitName.guard, host.getUnits().getAvailableCount(UnitName.guard));
        return units;
    }

    /**
     * the allied part of the defending army
     */
    UnitsMapDto alliedSnapshot()
    {
        var units = new UnitsMapDto();
        for (var unit : UnitName.getMilitaryUnits())
        {
            units.setCount(unit, garrisons.stream().mapToInt(garrison -> garrison.getUnits().getCount(unit)).sum());
        }
        return units;
    }

    List<KingdomReinforcementEntity> garrisons()
    {
        return garrisons;
    }
}
