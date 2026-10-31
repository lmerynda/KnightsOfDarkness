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
     * troops and guards at home, strengthened by towers (capped) and castles
     */
    public double getDefensePower(KingdomEntity kingdom)
    {
        var attackConfig = config.attack();
        var units = kingdom.getUnits();
        var basePower = UnitName.getMilitaryUnits().stream().mapToDouble(unit -> units.getAvailableCount(unit) * attackConfig.getDefensePower(unit)).sum();
        basePower += units.getAvailableCount(UnitName.guard) * attackConfig.getDefensePower(UnitName.guard);

        var buildings = kingdom.getBuildings();
        var towerBonus = Math.min(attackConfig.maxTowerDefenseBonus(), buildings.getCount(BuildingName.tower) * attackConfig.towerDefenseBonusPerTower());
        var castleBonus = buildings.getCount(BuildingName.castle) * attackConfig.castleDefenseBonusPerCastle();

        return basePower * (1 + towerBonus + castleBonus);
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
}
