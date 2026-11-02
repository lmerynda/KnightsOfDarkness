package com.knightsofdarkness.web.common.kingdom;

import java.util.Optional;

public enum SpecialBuildingType
{
    goldShaft, ironShaft, granary, forge, warehouse;

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
