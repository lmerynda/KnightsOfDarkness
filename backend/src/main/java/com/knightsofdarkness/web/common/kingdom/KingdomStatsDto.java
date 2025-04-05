package com.knightsofdarkness.web.common.kingdom;

import com.knightsofdarkness.web.kingdom.model.KingdomEntity;

public class KingdomStatsDto
{
    public String kingdomName;
    public int land;
    public int gold;
    public int army;

    public KingdomStatsDto()
    {
        this.kingdomName = "";
        this.land = 0;
        this.gold = 0;
        this.army = 0;
    }

    public static KingdomStatsDto fromKingdom(KingdomEntity kingdom)
    {
        var stats = new KingdomStatsDto();
        stats.kingdomName = kingdom.getName();
        stats.land = kingdom.getResources().getCount(ResourceName.land);
        stats.gold = kingdom.getResources().getCount(ResourceName.gold);
        stats.army = kingdom.getUnits().countAvailableMilitary();
        return stats;
    }
}
