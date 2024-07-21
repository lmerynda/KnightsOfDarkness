package com.knightsofdarkness.game.kingdom;

import java.util.EnumSet;

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
        KingdomBuildings buildingsBuilt = new KingdomBuildings();
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
}