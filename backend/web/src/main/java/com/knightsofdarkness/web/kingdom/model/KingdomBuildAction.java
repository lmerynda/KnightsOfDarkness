package com.knightsofdarkness.web.kingdom.model;

import com.knightsofdarkness.web.common.kingdom.BuildingName;
import com.knightsofdarkness.web.common.kingdom.KingdomBuildingsActionResult;
import com.knightsofdarkness.web.common.kingdom.KingdomBuildingsDto;
import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.utils.Utils;

public class KingdomBuildAction {
    private final KingdomEntity kingdom;
    private final GameConfig gameConfig;

    public KingdomBuildAction(KingdomEntity kingdom, GameConfig gameConfig)
    {
        this.kingdom = kingdom;
        this.gameConfig = gameConfig;
    }

    /**
     * simplest algorithm to build from top until the building points are depleted
     * 
     * @return the buildings that were built
     */
    public KingdomBuildingsActionResult build(KingdomBuildingsDto buildingsToBuild)
    {
        var buildingsBuilt = new KingdomBuildingsDto();
        for (var buildingName : BuildingName.values())
        {
            if (buildingsToBuild.getCount(buildingName) > 0)
            {
                var howManyWereBuilt = build(buildingName, buildingsToBuild.getCount(buildingName));
                buildingsBuilt.setCount(buildingName, howManyWereBuilt);
            }
        }

        return new KingdomBuildingsActionResult((Utils.format("Succesfully built {} buildings", buildingsBuilt.countAll())), buildingsBuilt);
    }

    int build(BuildingName building, int count)
    {
        // TODO tests
        var buildingCost = gameConfig.buildingPointCosts().getCost(building);
        var pointsToPutIntoBuilding = Math.min(kingdom.getResources().getCount(ResourceName.buildingPoints), count * buildingCost);
        var fullBuildings = pointsToPutIntoBuilding / buildingCost;
        var howManyToBuild = Math.min(fullBuildings, kingdom.getUnusedLand());
        assert howManyToBuild >= 0;
        kingdom.buildings.addCount(building, howManyToBuild);
        kingdom.resources.subtractCount(ResourceName.buildingPoints, howManyToBuild * buildingCost);

        return howManyToBuild;
    }

    public KingdomBuildingsActionResult demolish(KingdomBuildingsDto buildingsToDemolish)
    {
        var buildingsDemolished = new KingdomBuildingsDto();
        for (var buildingName : BuildingName.values())
        {
            var demolishCount = buildingsToDemolish.getCount(buildingName);
            if (demolishCount > 0)
            {
                // should building demolishing cost building points?
                var howManyToDemolish = Math.min(demolishCount, kingdom.buildings.getCount(buildingName));
                buildingsDemolished.setCount(buildingName, howManyToDemolish);
                kingdom.buildings.subtractCount(buildingName, howManyToDemolish);
            }
        }

        return new KingdomBuildingsActionResult(Utils.format("Succesfully demolished {} buildings", buildingsDemolished.countAll()), buildingsDemolished);
    }
}
