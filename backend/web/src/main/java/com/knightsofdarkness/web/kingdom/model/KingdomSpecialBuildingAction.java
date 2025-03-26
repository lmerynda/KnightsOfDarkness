package com.knightsofdarkness.web.kingdom.model;

import java.util.Optional;

import java.util.Collections;
import java.util.Comparator;
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
        if (kingdom.getSpecialBuildings().size() >= gameConfig.common().specialBuildingMaxCount())
        {
            return Optional.empty();
        }

        var specialBuilding = new KingdomSpecialBuildingEntity(Id.generate(), kingdom, buildingType, 0, 0, 10000, false);
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

    public int buildSpecialBuilding(KingdomSpecialBuildingEntity specialBuilding, int buildingPoints)
    {
        if (specialBuilding.isMaxLevel())
        {
            // log.info("[{}] special building {} is at max level", name, specialBuilding.getBuildingType());
            return 0;
        }
        // TODO encapsulate this functionality
        var buildingPointsRemaining = specialBuilding.buildingPointsRequired - specialBuilding.buildingPointsPut;
        var kingdomBuildingPoints = kingdom.getResources().getCount(ResourceName.buildingPoints);
        if (buildingPoints >= buildingPointsRemaining)
        {
            var buildingPointsToSpend = Math.min(kingdomBuildingPoints, buildingPointsRemaining);
            kingdom.getResources().subtractCount(ResourceName.buildingPoints, buildingPointsToSpend);
            specialBuilding.buildingPointsPut = 0;
            specialBuilding.level++;
            specialBuilding.buildingPointsRequired *= 2;
            if (specialBuilding.level >= 5)
            {
                specialBuilding.isMaxLevel = true;
                specialBuilding.buildingPointsRequired = 0;
            }
            return buildingPointsToSpend;
        } else
        {
            var buildingPointsToSpend = Math.min(kingdomBuildingPoints, buildingPoints);
            kingdom.getResources().subtractCount(ResourceName.buildingPoints, buildingPointsToSpend);
            specialBuilding.buildingPointsPut += buildingPointsToSpend;
            return buildingPointsToSpend;
        }
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
