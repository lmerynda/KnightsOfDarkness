package com.knightsofdarkness.game.gameconfig;

import com.knightsofdarkness.game.kingdom.UnitName;

public record TrainingCost(UnitTrainingCost goldMiner, UnitTrainingCost ironMiner, UnitTrainingCost farmer, UnitTrainingCost blacksmith, UnitTrainingCost builder, UnitTrainingCost carrier,
        UnitTrainingCost guard, UnitTrainingCost spy, UnitTrainingCost infantry, UnitTrainingCost bowmen, UnitTrainingCost cavalry) {
    public UnitTrainingCost getTrainingCost(UnitName name)
    {
        return switch (name)
        {
            case goldMiner -> goldMiner;
            case ironMiner -> ironMiner;
            case farmer -> farmer;
            case blacksmith -> blacksmith;
            case builder -> builder;
            case carrier -> carrier;
            case guard -> guard;
            case spy -> spy;
            case infantry -> infantry;
            case bowmen -> bowmen;
            case cavalry -> cavalry;
        };
    }
}