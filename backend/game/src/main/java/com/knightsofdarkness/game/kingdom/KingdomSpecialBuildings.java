package com.knightsofdarkness.game.kingdom;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Deprecated
public class KingdomSpecialBuildings {
    List<KingdomSpecialBuilding> specialBuildings;
    Kingdom kingdom;

    public KingdomSpecialBuildings(Kingdom kingdom)
    {
        specialBuildings = Arrays.asList(new KingdomSpecialBuilding(SpecialBuildingType.emptyBuilding), new KingdomSpecialBuilding(SpecialBuildingType.emptyBuilding), new KingdomSpecialBuilding(SpecialBuildingType.emptyBuilding),
                new KingdomSpecialBuilding(SpecialBuildingType.emptyBuilding), new KingdomSpecialBuilding(SpecialBuildingType.emptyBuilding));
    }

    public Optional<Integer> startNew(SpecialBuildingType specialBuilding, int buildingPlace)
    {
        int listIndex = buildingPlace - 1;
        // TODO move hardcoded value to config
        if (buildingPlace < 1 || buildingPlace > 5)
        {
            return Optional.empty();
        }

        if (specialBuildings.get(listIndex).buildingType != SpecialBuildingType.emptyBuilding)
        {
            return Optional.empty();
        }

        specialBuildings.set(listIndex, new KingdomSpecialBuilding(specialBuilding));

        return Optional.of(buildingPlace);
    }

    public void deleteBuilding(int buildingPlace)
    {
        int listIndex = buildingPlace - 1;
        // TODO move hardcoded value to config
        if (buildingPlace < 1 || buildingPlace > 5)
        {
            return;
        }

        specialBuildings.set(listIndex, new KingdomSpecialBuilding(SpecialBuildingType.emptyBuilding));
    }

    public Optional<KingdomSpecialBuilding> getAt(int buildingPlace)
    {
        if (buildingPlace < 1 || buildingPlace > 5)
        {
            return Optional.empty();
        }

        return Optional.of(specialBuildings.get(buildingPlace - 1));
    }

    public void build(int buildingPlace, int buildingPoints)
    {
        if (buildingPlace < 1 || buildingPlace > 5)
        {
            return;
        }

        var building = specialBuildings.get(buildingPlace - 1);

        if (building.level >= 5)
        {
            return;
        }

        int buildingCost = kingdom.getConfig().specialBuildingCosts().getCost(building.buildingType);
        int remainingCost = buildingCost - building.buildingPointsPut;
        int buildingPointsToSpend = Math.min(buildingPoints, kingdom.getResources().getCount(ResourceName.buildingPoints));

        if (buildingPointsToSpend >= remainingCost)
        {
            building.level++;
            building.buildingPointsPut = 0;
            return; // TODO indicate building points spent?
        }

        building.buildingPointsPut += buildingPointsToSpend;
        return;
    }
}
