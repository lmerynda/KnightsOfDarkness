package com.merynda.kod.gameconfig;

import com.merynda.kod.kingdom.UnitName;

public record KingdomStartingUnits(int goldMiner, int ironMiner, int builder, int blacksmith, int farmer, int carrier, int guard, int spy, int bowmen, int infantry, int cavalry) {
    public int getCount(UnitName name)
    {
        return switch (name)
        {
            case goldMiner -> goldMiner;
            case ironMiner -> ironMiner;
            case builder -> builder;
            case blacksmith -> blacksmith;
            case farmer -> farmer;
            case carrier -> carrier;
            case guard -> guard;
            case spy -> spy;
            case bowmen -> bowmen;
            case infantry -> infantry;
            case cavalry -> cavalry;
        };
    }
}
