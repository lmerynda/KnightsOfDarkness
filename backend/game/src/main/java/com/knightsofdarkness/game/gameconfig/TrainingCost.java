package com.knightsofdarkness.game.gameconfig;

import com.knightsofdarkness.game.kingdom.UnitName;

public record TrainingCost(UnitTrainingCost goldMiners, UnitTrainingCost ironMiners, UnitTrainingCost farmers, UnitTrainingCost blacksmiths, UnitTrainingCost builders, UnitTrainingCost carriers,
        UnitTrainingCost guards, UnitTrainingCost spies, UnitTrainingCost infantry, UnitTrainingCost bowmen, UnitTrainingCost cavalry) {
    public UnitTrainingCost getTrainingCost(UnitName name)
    {
        return switch (name)
        {
            case goldMiner -> goldMiners;
            case ironMiner -> ironMiners;
            case farmer -> farmers;
            case blacksmith -> blacksmiths;
            case builder -> builders;
            case carrier -> carriers;
            case guard -> guards;
            case spy -> spies;
            case infantry -> infantry;
            case bowmen -> bowmen;
            case cavalry -> cavalry;
        };
    }
}