package com.knightsofdarkness.web.kingdom.model;

import com.knightsofdarkness.web.common.kingdom.BuildingName;
import com.knightsofdarkness.web.common.kingdom.KingdomUnitsActionResult;
import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.kingdom.UnitName;
import com.knightsofdarkness.web.common.kingdom.UnitsMapDto;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.game.config.UnitTrainingCost;
import com.knightsofdarkness.web.utils.Utils;

public class KingdomTrainAction {
    private final KingdomEntity kingdom;
    private final GameConfig gameConfig;

    public KingdomTrainAction(KingdomEntity kingdom, GameConfig gameConfig)
    {
        this.kingdom = kingdom;
        this.gameConfig = gameConfig;
    }

    public KingdomUnitsActionResult train(UnitsMapDto unitsToTrain)
    {
        var trainedUnits = new UnitsMapDto();

        for (var unitName : UnitName.values())
        {
            var trainingCost = gameConfig.trainingCost().getTrainingCost(unitName);
            var maximumToAfford = calculateMaximumToAfford(kingdom.getResources(), trainingCost);
            var howManyToTrain = Math.min(unitsToTrain.getCount(unitName), maximumToAfford);
            var maybeBuildingType = BuildingName.getByUnit(unitName);
            if (maybeBuildingType.isPresent())
            {
                var buildingType = maybeBuildingType.get();
                var buildingCapacity = gameConfig.buildingCapacity().getCapacity(buildingType);
                var buildingOccupancy = kingdom.units.getTotalCount(unitName);
                var freeCapacity = kingdom.buildings.getCount(buildingType) * buildingCapacity - buildingOccupancy;
                howManyToTrain = Math.max(0, Math.min(howManyToTrain, freeCapacity));
            }

            kingdom.resources.subtractCount(ResourceName.gold, howManyToTrain * trainingCost.gold());
            kingdom.resources.subtractCount(ResourceName.tools, howManyToTrain * trainingCost.tools());
            kingdom.resources.subtractCount(ResourceName.weapons, howManyToTrain * trainingCost.weapons());
            kingdom.resources.subtractCount(ResourceName.unemployed, howManyToTrain);
            kingdom.units.addCount(unitName, howManyToTrain);
            trainedUnits.setCount(unitName, howManyToTrain);
        }

        return new KingdomUnitsActionResult(Utils.format("Succesfully trained {} units", trainedUnits.countAll()), trainedUnits);
    }

    public KingdomUnitsActionResult fireUnits(UnitsMapDto unitsToFire)
    {
        var firedUnits = new UnitsMapDto();
        var kingdomUnits = kingdom.units;

        for (var unitName : UnitName.values())
        {
            var howManyToFire = Math.min(unitsToFire.getCount(unitName), kingdomUnits.getAvailableCount(unitName));

            kingdom.resources.subtractCount(ResourceName.unemployed, howManyToFire);
            kingdomUnits.subtractAvailableCount(unitName, howManyToFire);
            firedUnits.setCount(unitName, howManyToFire);
        }

        return new KingdomUnitsActionResult(Utils.format("Succesfully fired {} units", firedUnits.countAll()), firedUnits);
    }

    private int calculateMaximumToAfford(KingdomResourcesEntity resources, UnitTrainingCost trainingCost)
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
