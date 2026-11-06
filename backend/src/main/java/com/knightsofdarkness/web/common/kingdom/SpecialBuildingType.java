package com.knightsofdarkness.web.common.kingdom;

import java.util.Optional;

public enum SpecialBuildingType
{
    goldShaft, ironShaft, granary, forge, warehouse;

    /**
     * a kingdom raises one great work of each kind, so production bonuses never pile up; warehouses alone may be raised
     * more than once, since their shields add up to a cap one warehouse cannot reach
     */
    public boolean mayBeRaisedTwice()
    {
        return this == warehouse;
    }

    /**
     * the great work that raises a resource's production, if any does
     */
    public static Optional<SpecialBuildingType> fromResource(ResourceName resourceName)
    {
        return switch (resourceName)
        {
            case gold -> Optional.of(goldShaft);
            case iron -> Optional.of(ironShaft);
            case food -> Optional.of(granary);
            case tools, weapons -> Optional.of(forge);
            default -> Optional.empty();
        };
    }
}
