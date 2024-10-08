package com.knightsofdarkness.game.kingdom;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

import com.knightsofdarkness.common.kingdom.UnitName;

public class KingdomUnitsOnTheMove {
    UUID id;
    String kingdomName;
    String targetKingdomName;
    int turnsLeft;
    Map<UnitName, Integer> units = new EnumMap<>(UnitName.class);

    public KingdomUnitsOnTheMove()
    {
        for (var name : UnitName.values())
        {
            units.put(name, 0);
        }
    }
}
