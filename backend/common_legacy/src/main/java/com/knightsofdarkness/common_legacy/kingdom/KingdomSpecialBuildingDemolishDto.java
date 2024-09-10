package com.knightsofdarkness.common_legacy.kingdom;

import java.util.UUID;

public class KingdomSpecialBuildingDemolishDto {
    public UUID id;

    public KingdomSpecialBuildingDemolishDto()
    {
    }

    public KingdomSpecialBuildingDemolishDto(UUID id)
    {
        this.id = id;
    }

    public String toString()
    {
        return "KingdomSpecialBuildingDemolishDto{" +
                "id=" + id +
                '}';
    }
}
