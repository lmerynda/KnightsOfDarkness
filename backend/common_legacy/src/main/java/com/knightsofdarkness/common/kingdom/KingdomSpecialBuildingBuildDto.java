package com.knightsofdarkness.common.kingdom;

import java.util.UUID;

public class KingdomSpecialBuildingBuildDto {
    public UUID id;
    public int buildingPoints;

    public KingdomSpecialBuildingBuildDto()
    {
    }

    public KingdomSpecialBuildingBuildDto(UUID id, int buildingPoints)
    {
        this.id = id;
        this.buildingPoints = buildingPoints;
    }

    public String toString()
    {
        return "KingdomSpecialBuildingBuildDto{" +
                "id=" + id +
                ", buildingPoints=" + buildingPoints +
                '}';
    }
}
