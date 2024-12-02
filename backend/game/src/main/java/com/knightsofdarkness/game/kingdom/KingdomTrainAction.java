package com.knightsofdarkness.game.kingdom;

import com.knightsofdarkness.common.kingdom.BuildingName;
import com.knightsofdarkness.common.kingdom.KingdomUnitsActionResult;
import com.knightsofdarkness.common.kingdom.ResourceName;
import com.knightsofdarkness.common.kingdom.UnitName;
import com.knightsofdarkness.common.kingdom.UnitsMapDto;
import com.knightsofdarkness.game.Utils;
import com.knightsofdarkness.game.gameconfig.UnitTrainingCost;

public class KingdomTrainAction {
    private final Kingdom kingdom;

    public KingdomTrainAction(Kingdom kingdom)
    {
        this.kingdom = kingdom;
    }

    public KingdomUnitsActionResult train(UnitsMapDto unitsToTrain)
    {
        var trainedUnits = new UnitsMapDto();

        for (var unitName : UnitName.values())
        {
            var trainingCost = kingdom.getConfig().trainingCost().getTrainingCost(unitName);
            var maximumToAfford = calculateMaximumToAfford(kingdom.getResources(), trainingCost);
            var howManyToTrain = Math.min(unitsToTrain.getCount(unitName), maximumToAfford);
            var maybeBuildingType = BuildingName.getByUnit(unitName);
            if (maybeBuildingType.isPresent())
            {
                var buildingType = maybeBuildingType.get();
                var buildingCapacity = kingdom.getConfig().buildingCapacity().getCapacity(buildingType);
                var buildingOccupancy = kingdom.getUnits().getCount(unitName);
                var freeCapacity = kingdom.getBuildings().getCount(buildingType) * buildingCapacity - buildingOccupancy;
                howManyToTrain = Math.max(0, Math.min(howManyToTrain, freeCapacity));
            }

            kingdom.getResources().subtractCount(ResourceName.gold, howManyToTrain * trainingCost.gold());
            kingdom.getResources().subtractCount(ResourceName.tools, howManyToTrain * trainingCost.tools());
            kingdom.getResources().subtractCount(ResourceName.weapons, howManyToTrain * trainingCost.weapons());
            kingdom.getResources().subtractCount(ResourceName.unemployed, howManyToTrain);
            kingdom.getUnits().addCount(unitName, howManyToTrain);
            trainedUnits.setCount(unitName, howManyToTrain);
        }

        return new KingdomUnitsActionResult(Utils.format("Succesfully trained {} units", trainedUnits.countAll()), trainedUnits);
    }

    private int calculateMaximumToAfford(KingdomResources resources, UnitTrainingCost trainingCost)
    {
        int gold = resources.getCount(ResourceName.gold);
        int tools = resources.getCount(ResourceName.tools);
        int weapons = resources.getCount(ResourceName.weapons);
        int unemployed = resources.getCount(ResourceName.unemployed);

        var maxForGold = gold / trainingCost.gold();
        var maxForEquipment = trainingCost.tools() > 0 ? tools / trainingCost.tools() : weapons / trainingCost.weapons();

        return Math.min(Math.min(maxForGold, maxForEquipment), unemployed);
    }
}
