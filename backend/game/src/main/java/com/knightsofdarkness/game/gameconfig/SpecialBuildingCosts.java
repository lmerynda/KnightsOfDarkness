package com.knightsofdarkness.game.gameconfig;

import com.knightsofdarkness.game.kingdom.SpecialBuildingType;

public record SpecialBuildingCosts(int goldShaft, int ironShaft, int granary, int forge, int warehouse) {

    public int getCost(SpecialBuildingType name)
    {
        return switch (name)
        {
            case goldShaft -> goldShaft;
            case ironShaft -> ironShaft;
            case granary -> granary;
            case forge -> forge;
            case warehouse -> warehouse;
            default -> throw new RuntimeException("unknown cost for special building " + name);
        };
    }
}
