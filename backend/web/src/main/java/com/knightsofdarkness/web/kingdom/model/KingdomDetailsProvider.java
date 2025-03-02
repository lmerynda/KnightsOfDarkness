package com.knightsofdarkness.web.kingdom.model;

import org.springframework.stereotype.Service;

import com.knightsofdarkness.common.kingdom.BuildingName;
import com.knightsofdarkness.common.kingdom.ResourceName;
import com.knightsofdarkness.common.kingdom.UnitName;
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

    public int getFoodUpkeep(KingdomEntity kingdom)
    {
        return kingdom.units.countAll() * config.common().foodUpkeepPerUnit();
    }
}
