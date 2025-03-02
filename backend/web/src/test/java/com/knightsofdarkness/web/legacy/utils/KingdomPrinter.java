package com.knightsofdarkness.game.utils;

import com.knightsofdarkness.common.kingdom.BuildingName;
import com.knightsofdarkness.common.kingdom.ResourceName;
import com.knightsofdarkness.common.kingdom.UnitName;
import com.knightsofdarkness.web.kingdom.legacy.Kingdom;

public class KingdomPrinter {
    public static void printResourcesHeader()
    {
        System.out.format("Kingdom         | land  |   ppl  | unemployed | builders |    farmers    |  blacksmiths  |    goldMiner   |   ironMiner  |    gold    |    food    |    tools   |    iron    | marketOffers\n");
    }

    public static void printLineSeparator()
    {
        System.out.format("==========================================================================================================================================================================================\n");
    }

    public static void kingdomInfoPrinter(Kingdom kingdom)
    {
        var resources = kingdom.getResources();
        var units = kingdom.getUnits();
        // @formatter:off 
        System.out.format("%-15s | %5d | %6d | %10d | %8d | %6d/%-6d | %6d/%-6d | %7d/%-7d | %6d/%-6d | %10d | %10d | %10d | %10d\n", 
                    kingdom.getName(),
            resources.getCount(ResourceName.land),
            kingdom.getTotalPeopleCount(),
            resources.getCount(ResourceName.unemployed),
            units.getTotalCount(UnitName.builder),
            units.getTotalCount(UnitName.farmer),
            kingdom.getBuildingCapacity(BuildingName.farm),
            units.getTotalCount(UnitName.blacksmith),
            kingdom.getBuildingCapacity(BuildingName.workshop),
            units.getTotalCount(UnitName.goldMiner),
            kingdom.getBuildingCapacity(BuildingName.goldMine),
            units.getTotalCount(UnitName.ironMiner),
            kingdom.getBuildingCapacity(BuildingName.ironMine),
            resources.getCount(ResourceName.gold),
            resources.getCount(ResourceName.food),
            resources.getCount(ResourceName.tools),
            resources.getCount(ResourceName.iron)
        );
        // @formatter:on
    }

    public static void printTurnInfo(int currentTurn)
    {
        System.out.format("Turn %d\n", currentTurn);
    }
}
