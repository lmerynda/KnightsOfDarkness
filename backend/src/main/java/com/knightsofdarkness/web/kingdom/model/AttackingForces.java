package com.knightsofdarkness.web.kingdom.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.knightsofdarkness.web.common.kingdom.UnitName;
import com.knightsofdarkness.web.common.kingdom.UnitsMapDto;

/**
 * Everyone who attacks in one battle: the army that set off the strike and the allies' armies that joined it, fighting as
 * one army. Losses of each unit type are shared in proportion to how many of that type each army brought, and what the
 * battle wins is shared in proportion to the attack power each army brought.
 */
final class AttackingForces
{
    /** one kingdom's army in the battle, with what it set off with */
    record Army(KingdomEntity kingdom, KingdomOngoingAttackEntity attack, UnitsMapDto sent)
    {
        String name()
        {
            return kingdom.getName();
        }
    }

    private final List<Army> armies;

    private AttackingForces(List<Army> armies)
    {
        this.armies = armies;
    }

    /** the army that set off the strike first, then the armies that joined it */
    static AttackingForces of(KingdomEntity lead, KingdomOngoingAttackEntity strike, List<KingdomOngoingAttackEntity> joined)
    {
        var armies = new ArrayList<Army>();
        armies.add(new Army(lead, strike, new UnitsMapDto(strike.units)));
        joined.forEach(attack -> armies.add(new Army(attack.getKingdom(), attack, new UnitsMapDto(attack.units))));
        return new AttackingForces(List.copyOf(armies));
    }

    static AttackingForces alone(KingdomEntity kingdom, KingdomOngoingAttackEntity attack)
    {
        return of(kingdom, attack, List.of());
    }

    List<Army> armies()
    {
        return armies;
    }

    Army lead()
    {
        return armies.get(0);
    }

    int count(UnitName unit)
    {
        return armies.stream().mapToInt(army -> army.attack().units.getCount(unit)).sum();
    }

    /** the whole attacking army as it stands now */
    UnitsMapDto units()
    {
        var units = new UnitsMapDto();
        for (var unit : UnitName.getMilitaryUnits())
        {
            units.setCount(unit, count(unit));
        }
        return units;
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
     * every army loses its share rounded down, and whatever rounding leaves falls on the armies that still have the most;
     * the fallen leave their kingdom's army on the march
     */
    void lose(UnitName unit, int count)
    {
        var total = count(unit);
        if (count <= 0 || total <= 0)
        {
            return;
        }

        var toLose = Math.min(count, total);
        var lost = share(toLose, armies.stream().mapToDouble(army -> army.attack().units.getCount(unit)).toArray(), armies.stream().mapToInt(army -> army.attack().units.getCount(unit)).toArray());
        for (int index = 0; index < armies.size(); index++)
        {
            var army = armies.get(index);
            army.attack().units.subtractCount(unit, lost[index]);
            army.kingdom().getUnits().subtractMobileCount(unit, lost[index]);
        }
    }

    /**
     * each army's share of what the battle won, by the attack power it set off with
     */
    double[] weights(KingdomDetailsProvider details)
    {
        var powers = armies.stream().mapToDouble(army -> details.getAttackPower(army.sent())).toArray();
        var total = Arrays.stream(powers).sum();
        if (total <= 0)
        {
            var even = new double[armies.size()];
            Arrays.fill(even, 1.0);
            return even;
        }
        return powers;
    }

    /**
     * splits a whole count by the weights: each part rounded down, and what rounding leaves going to the largest remainders,
     * never giving a part more than its cap
     */
    static int[] share(int whole, double[] weights, int[] caps)
    {
        var parts = new int[weights.length];
        var total = Arrays.stream(weights).sum();
        if (whole <= 0 || total <= 0)
        {
            return parts;
        }
        var remainders = new double[weights.length];
        var assigned = 0;
        for (int index = 0; index < weights.length; index++)
        {
            var exact = whole * weights[index] / total;
            parts[index] = Math.min(caps[index], (int) Math.floor(exact));
            remainders[index] = exact - parts[index];
            assigned += parts[index];
        }
        while (assigned < whole)
        {
            var best = -1;
            for (int index = 0; index < weights.length; index++)
            {
                if (parts[index] < caps[index] && (best < 0 || remainders[index] > remainders[best]))
                {
                    best = index;
                }
            }
            if (best < 0)
            {
                break;
            }
            parts[best]++;
            remainders[best] -= 1;
            assigned++;
        }
        return parts;
    }
}
