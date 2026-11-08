package com.knightsofdarkness.web.kingdom.model;

import org.springframework.stereotype.Service;

import com.knightsofdarkness.web.common.kingdom.BuildingName;
import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.kingdom.UnitName;
import com.knightsofdarkness.web.common.kingdom.UnitsMapDto;
import com.knightsofdarkness.web.game.config.GameConfig;

@Service
public class KingdomDetailsProvider {
    private final GameConfig config;

    public KingdomDetailsProvider(GameConfig gameConfig)
    {
        this.config = gameConfig;
    }

    public boolean hasMaxTurns(KingdomEntity kingdom)
    {
        return kingdom.resources.getCount(ResourceName.turns) >= config.common().maxTurns();
    }

    public int getBuildingCapacity(KingdomEntity kingdom, BuildingName name)
    {
        return kingdom.buildings.getCapacity(name, config.buildingCapacity().getCapacity(name));
    }

    public int getIronUpkeep(KingdomEntity kingdom, double nourishmentProductionFactor)
    {
        // unfed blacksmith who don't work, will not consume any iron either
        double production = kingdom.units.getAvailableCount(UnitName.blacksmith) * nourishmentProductionFactor * config.production().getProductionRate(UnitName.blacksmith) * config.common().ironConsumptionPerProductionUnit();

        return ((int) Math.ceil(production));
    }

    public double getAttackPower(UnitsMapDto units)
    {
        var attackConfig = config.attack();
        return UnitName.getMilitaryUnits().stream().mapToDouble(unit -> units.getCount(unit) * attackConfig.getAttackPower(unit)).sum();
    }

    /**
     * troops and guards at home and allied garrisons standing with them; soldiers with a place in a castle stand stronger
     */
    public double getDefensePower(KingdomEntity kingdom)
    {
        var attackConfig = config.attack();
        var forces = new DefendingForces(kingdom);
        var soldiers = UnitName.getMilitaryUnits().stream().mapToDouble(unit -> forces.count(unit) * attackConfig.getDefensePower(unit)).sum();
        var sheltered = forces.shelteredInCastles(getBuildingCapacity(kingdom, BuildingName.castle));
        var shelteredPower = sheltered.entrySet().stream().mapToDouble(entry -> entry.getValue() * attackConfig.getDefensePower(entry.getKey())).sum();
        var guards = kingdom.getUnits().getAvailableCount(UnitName.guard) * attackConfig.getDefensePower(UnitName.guard);

        return soldiers + shelteredPower * attackConfig.castleSoldierBonus() + guards;
    }

    /**
     * the arrows a kingdom's bowmen loose in the salvo: one each, more for those with a place in a tower
     */
    public int getSalvoArrows(KingdomEntity kingdom)
    {
        var bowmen = new DefendingForces(kingdom).count(UnitName.bowman);
        var onTowers = Math.min(bowmen, getBuildingCapacity(kingdom, BuildingName.tower));
        return bowmen + (int) Math.round(onTowers * config.attack().towerFireBonus());
    }

    public double getCounterIntelligence(KingdomEntity kingdom)
    {
        var espionage = config.espionage();
        var units = kingdom.getUnits();
        return units.getAvailableCount(UnitName.spy) * espionage.counterIntelligencePerSpy() + units.getAvailableCount(UnitName.guard) * espionage.counterIntelligencePerGuard();
    }

    public int getHousingCapacity(KingdomEntity kingdom)
    {
        return config.common().kingdomBaseHousingCapacity() + kingdom.getBuildings().getCount(BuildingName.house) * config.buildingCapacity().getCapacity(BuildingName.house);
    }

    public int getFoodUpkeep(KingdomEntity kingdom)
    {
        return kingdom.units.countAll() * config.common().foodUpkeepPerUnit();
    }

    /**
     * the gold soldiers are paid each turn, a share of what training them cost; soldiers away on the march or standing guard
     * with an ally are paid too
     */
    public int getGoldUpkeep(KingdomEntity kingdom)
    {
        var upkeep = UnitName.getMilitaryUnits().stream()
                .mapToDouble(unit -> (double) kingdom.getUnits().getTotalCount(unit) * config.trainingCost().getTrainingCost(unit).gold())
                .sum();
        return (int) Math.round(upkeep * config.common().soldierUpkeepRatio());
    }
}
