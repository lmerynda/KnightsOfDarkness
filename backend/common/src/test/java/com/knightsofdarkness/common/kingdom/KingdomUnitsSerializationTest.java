package com.knightsofdarkness.common.kingdom;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.EnumMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class KingdomUnitsSerializationTest {
    @Test
    void serializationTest()
    {
        var availableUnits = createUnitsMap();
        var mobileUnits = createUnitsMap();

        var kingdomUnits = new KingdomUnitsDto(availableUnits, mobileUnits);

        assertEquals(TestUtils.emptyKingdomUnitsJson(), TestUtils.kingdomUnitsDtoToJson(kingdomUnits));
    }

    @Test
    void deserializationTest()
    {
        var kingdomUnitsDto = TestUtils.jsonToKingdomUnitsDto(TestUtils.emptyKingdomUnitsJson());
        assertEquals(new KingdomUnitsDto().toString(), kingdomUnitsDto.toString());
    }

    private Map<UnitName, Integer> createUnitsMap()
    {
        var units = new EnumMap<UnitName, Integer>(UnitName.class);
        units.put(UnitName.goldMiner, 0);
        units.put(UnitName.ironMiner, 0);
        units.put(UnitName.farmer, 0);
        units.put(UnitName.blacksmith, 0);
        units.put(UnitName.builder, 0);
        units.put(UnitName.carrier, 0);
        units.put(UnitName.guard, 0);
        units.put(UnitName.spy, 0);
        units.put(UnitName.infantry, 0);
        units.put(UnitName.bowman, 0);
        units.put(UnitName.cavalry, 0);
        return units;
    }
}
