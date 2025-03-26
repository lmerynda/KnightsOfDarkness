package com.knightsofdarkness.web.game.config;

import com.knightsofdarkness.web.common.kingdom.UnitName;

public record TrainingCost(UnitTrainingCost goldMiner, UnitTrainingCost ironMiner, UnitTrainingCost farmer, UnitTrainingCost blacksmith, UnitTrainingCost builder, UnitTrainingCost carrier,
        UnitTrainingCost guard, UnitTrainingCost spy, UnitTrainingCost infantry, UnitTrainingCost bowman, UnitTrainingCost cavalry) {
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
            case bowman -> bowman;
            case cavalry -> cavalry;
        };
    }
}
