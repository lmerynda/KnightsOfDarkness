package com.merynda.kod.utils;

import com.merynda.kod.kingdom.BuildingName;
import com.merynda.kod.kingdom.Kingdom;
import com.merynda.kod.kingdom.ResourceName;
import com.merynda.kod.kingdom.UnitName;

public class KingdomPrinter {
    public static void printResourcesHeader()
    {
        System.out.format("Kingdom         | land  |   ppl  | unemployed | builders |    farmers    |  blacksmiths  |    goldMiners   |   ironMiners  |    gold    |    food    |    tools   |    iron    | marketOffers\n");
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
        System.out.format("%-15s | %5d | %6d | %10d | %8d | %6d/%-6d | %6d/%-6d | %7d/%-7d | %6d/%-6d | %10d | %10d | %10d | %10d | %2d\n", 
            kingdom.getName(),
            resources.getCount(ResourceName.land),
            kingdom.getTotalPeopleCount(),
            resources.getCount(ResourceName.unemployed),
            units.getCount(UnitName.builder),
            units.getCount(UnitName.farmer),
            kingdom.getBuildingCapacity(BuildingName.farm),
            units.getCount(UnitName.blacksmith),
            kingdom.getBuildingCapacity(BuildingName.workshop),
            units.getCount(UnitName.goldMiner),
            kingdom.getBuildingCapacity(BuildingName.goldMine),
            units.getCount(UnitName.ironMiner),
            kingdom.getBuildingCapacity(BuildingName.ironMine),
            resources.getCount(ResourceName.gold),
            resources.getCount(ResourceName.food),
            resources.getCount(ResourceName.tools),
            resources.getCount(ResourceName.iron),
            kingdom.getMarketOffers().size()
        );
        // @formatter:on
    }

    public static void printTurnInfo(int currentTurn)
    {
        System.out.format("Turn %d\n", currentTurn);
    }
}
