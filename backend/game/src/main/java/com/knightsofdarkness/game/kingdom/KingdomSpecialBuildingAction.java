package com.knightsofdarkness.game.kingdom;

import java.util.Optional;

import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;

import com.knightsofdarkness.common.kingdom.ResourceName;
import com.knightsofdarkness.common.kingdom.SpecialBuildingType;

public class KingdomSpecialBuildingAction {
    private final Kingdom kingdom;

    public KingdomSpecialBuildingAction(Kingdom kingdom)
    {
        this.kingdom = kingdom;
    }

    public Optional<KingdomSpecialBuilding> startSpecialBuilding(SpecialBuildingType name)
    {
        if (kingdom.getSpecialBuildings().size() >= kingdom.getConfig().common().specialBuildingMaxCount())
        {
            return Optional.empty();
        }

        var specialBuilding = new KingdomSpecialBuilding(name);
        kingdom.getSpecialBuildings().add(specialBuilding);

        return Optional.of(specialBuilding);
    }

    public Optional<KingdomSpecialBuilding> getLowestLevelSpecialBuilding()
    {
        if (kingdom.getSpecialBuildings().isEmpty())
        {
            return Optional.empty();
        }

        return Optional.of(Collections.min(kingdom.getSpecialBuildings(), Comparator.comparingInt(KingdomSpecialBuilding::getLevel)));
    }

    public int buildSpecialBuilding(KingdomSpecialBuilding specialBuilding, int buildingPoints)
    {
        if (specialBuilding.isMaxLevel())
        {
            // log.info("[{}] special building {} is at max level", name, specialBuilding.getBuildingType());
            return 0;
        }
        // TODO encapsulate this functionality
        var buildingPointsRemaining = specialBuilding.getBuildingPointsRequired() - specialBuilding.getBuildingPointsPut();
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
        var specialBuilding = kingdom.getSpecialBuildings().stream().filter(sb -> sb.getId().equals(id)).findFirst();
        if (specialBuilding.isEmpty())
        {
            return false;
        }

        kingdom.getSpecialBuildings().remove(specialBuilding.get());
        return true;
    }
}
