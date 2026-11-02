package com.knightsofdarkness.web.kingdom.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.kingdom.SpecialBuildingType;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.utils.Id;

public class KingdomSpecialBuildingAction {
    private final KingdomEntity kingdom;
    private final GameConfig gameConfig;

    public KingdomSpecialBuildingAction(KingdomEntity kingdom, GameConfig gameConfig)
    {
        this.kingdom = kingdom;
        this.gameConfig = gameConfig;
    }

    public Optional<KingdomSpecialBuildingEntity> startSpecialBuilding(SpecialBuildingType buildingType)
    {
        if (buildingType == null)
        {
            return Optional.empty();
        }

        if (kingdom.getSpecialBuildings().size() >= gameConfig.common().specialBuildingMaxCount())
        {
            return Optional.empty();
        }

        var firstLevelCost = gameConfig.specialBuildingCosts().getCost(buildingType);
        var specialBuilding = new KingdomSpecialBuildingEntity(Id.generate(), kingdom, buildingType, 0, 0, firstLevelCost, false);
        kingdom.getSpecialBuildings().add(specialBuilding);

        return Optional.of(specialBuilding);
    }

    public Optional<KingdomSpecialBuildingEntity> getLowestLevelSpecialBuilding()
    {
        if (kingdom.getSpecialBuildings().isEmpty())
        {
            return Optional.empty();
        }

        return Optional.of(Collections.min(kingdom.getSpecialBuildings(), Comparator.comparingInt(specialBuilding -> specialBuilding.level)));
    }

    /**
     * puts building points into the building, completing at most one level, every level costs twice as much as the previous one
     *
     * @return building points spent
     */
    public int buildSpecialBuilding(KingdomSpecialBuildingEntity specialBuilding, int buildingPoints)
    {
        if (specialBuilding.isMaxLevel())
        {
            return 0;
        }

        var spendable = Math.max(0, Math.min(buildingPoints, kingdom.getResources().getCount(ResourceName.buildingPoints)));
        var buildingPointsRemaining = specialBuilding.buildingPointsRequired - specialBuilding.buildingPointsPut;
        if (spendable < buildingPointsRemaining)
        {
            kingdom.getResources().subtractCount(ResourceName.buildingPoints, spendable);
            specialBuilding.buildingPointsPut += spendable;
            return spendable;
        }

        kingdom.getResources().subtractCount(ResourceName.buildingPoints, buildingPointsRemaining);
        specialBuilding.buildingPointsPut = 0;
        specialBuilding.level++;
        specialBuilding.buildingPointsRequired *= 2;
        if (specialBuilding.level >= gameConfig.common().specialBuildingMaxLevel())
        {
            specialBuilding.isMaxLevel = true;
            specialBuilding.buildingPointsRequired = 0;
        }
        return buildingPointsRemaining;
    }

    public boolean demolishSpecialBuilding(UUID id)
    {
        var specialBuilding = kingdom.getSpecialBuildings().stream().filter(sb -> sb.id.equals(id)).findFirst();
        if (specialBuilding.isEmpty())
        {
            return false;
        }

        kingdom.getSpecialBuildings().remove(specialBuilding.get());
        return true;
    }
}
