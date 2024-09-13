package com.knightsofdarkness.game.kingdom;

import java.util.EnumSet;

import com.knightsofdarkness.common.kingdom.BuildingName;
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
    public KingdomBuildings build(KingdomBuildings buildingsToBuild)
    {
        var buildingsBuilt = new KingdomBuildings();
        // by using EnumSet we make sure the names are ordered as specified in the enum declaration
        var buildingNames = EnumSet.copyOf(buildingsToBuild.buildings.keySet());
        for (var buildingName : buildingNames)
        {
            if(buildingsToBuild.getCount(buildingName) > 0)
            {
                var howManyWereBuilt = build(buildingName, buildingsToBuild.getCount(buildingName));
                buildingsBuilt.addCount(buildingName, howManyWereBuilt);
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

    public KingdomBuildings demolish(KingdomBuildings buildingsToDemolish)
    {
        var buildingsDemolished = new KingdomBuildings();
        var buildingNames = EnumSet.copyOf(buildingsToDemolish.buildings.keySet());
        for (var buildingName : buildingNames)
        {
            var demolishCount = buildingsToDemolish.getCount(buildingName);
            if (demolishCount > 0)
            {
                // should building demolishing cost building points?
                var howManyToDemolish = Math.min(demolishCount, kingdom.getBuildings().getCount(buildingName));
                buildingsDemolished.addCount(buildingName, howManyToDemolish);
                kingdom.getBuildings().subtractCount(buildingName, howManyToDemolish);
            }
        }

        return buildingsDemolished;
    }
}
