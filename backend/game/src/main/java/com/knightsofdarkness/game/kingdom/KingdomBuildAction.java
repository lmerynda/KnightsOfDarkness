package com.knightsofdarkness.game.kingdom;

import com.knightsofdarkness.common.kingdom.BuildingName;
import com.knightsofdarkness.common.kingdom.KingdomBuildingsDto;
import com.knightsofdarkness.common.kingdom.ResourceName;

public class KingdomBuildAction {
    private final Kingdom kingdom;

    public KingdomBuildAction(Kingdom kingdom)
    {
        this.kingdom = kingdom;
    }

    /**
     * simplest algorithm to build from top until the building points are depleted
     * 
     * @return the buildings that were built
     */
    public KingdomBuildingsDto build(KingdomBuildingsDto buildingsToBuild)
    {
        var buildingsBuilt = new KingdomBuildingsDto();
        for (var buildingName : BuildingName.values())
        {
            if(buildingsToBuild.getCount(buildingName) > 0)
            {
                var howManyWereBuilt = build(buildingName, buildingsToBuild.getCount(buildingName));
                buildingsBuilt.setCount(buildingName, howManyWereBuilt);
            }
        }

        return buildingsBuilt;
    }

    public int build(BuildingName building, int count)
    {
        // TODO tests
        var buildingCost = kingdom.getConfig().buildingPointCosts().getCost(building);
        var pointsToPutIntoBuilding = Math.min(kingdom.getResources().getCount(ResourceName.buildingPoints), count * buildingCost);
        var fullBuildings = pointsToPutIntoBuilding / buildingCost;
        var howManyToBuild = Math.min(fullBuildings, kingdom.getUnusedLand());
        assert howManyToBuild >= 0;
        kingdom.getBuildings().addCount(building, howManyToBuild);
        kingdom.getResources().subtractCount(ResourceName.buildingPoints, howManyToBuild * buildingCost);

        return howManyToBuild;
    }

    public KingdomBuildingsDto demolish(KingdomBuildingsDto buildingsToDemolish)
    {
        var buildingsDemolished = new KingdomBuildingsDto();
        for (var buildingName : BuildingName.values())
        {
            var demolishCount = buildingsToDemolish.getCount(buildingName);
            if (demolishCount > 0)
            {
                // should building demolishing cost building points?
                var howManyToDemolish = Math.min(demolishCount, kingdom.getBuildings().getCount(buildingName));
                buildingsDemolished.setCount(buildingName, howManyToDemolish);
                kingdom.getBuildings().subtractCount(buildingName, howManyToDemolish);
            }
        }

        return buildingsDemolished;
    }
}
