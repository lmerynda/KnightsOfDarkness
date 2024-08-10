package com.knightsofdarkness.common;

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
