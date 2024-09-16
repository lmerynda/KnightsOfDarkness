package com.knightsofdarkness.game.gameconfig;

import com.knightsofdarkness.common.kingdom.UnitName;

public record KingdomStartingUnits(int goldMiner, int ironMiner, int builder, int blacksmith, int farmer, int carrier, int guard, int spy, int bowman, int infantry, int cavalry) {
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
            case bowman -> bowman;
            case infantry -> infantry;
            case cavalry -> cavalry;
        };
    }
}
