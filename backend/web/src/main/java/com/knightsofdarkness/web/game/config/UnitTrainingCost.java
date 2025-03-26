package com.knightsofdarkness.web.game.config;

import com.knightsofdarkness.web.common.kingdom.ResourceName;

public record UnitTrainingCost(int gold, int tools, int weapons) {
    public int getTrainingCost(ResourceName name)
    {
        return switch (name)
        {
            case gold -> gold;
            case tools -> tools;
            case weapons -> weapons;
            default -> throw new RuntimeException("Invalid training cost resource: " + name);
        };
    }
}
