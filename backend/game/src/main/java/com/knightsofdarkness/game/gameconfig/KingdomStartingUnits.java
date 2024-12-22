package com.knightsofdarkness.game.gameconfig;

import com.knightsofdarkness.common.kingdom.KingdomUnitsDto;
import com.knightsofdarkness.common.kingdom.UnitName;
import com.knightsofdarkness.common.kingdom.UnitsMapDto;

public record KingdomStartingUnits(int goldMiner, int ironMiner, int builder, int blacksmith, int farmer, int carrier, int guard, int spy, int bowman, int infantry, int cavalry) {
    public int getCount(UnitName name)
    {
        return switch (name)
        {
            case goldMiner -> goldMiner;
            case ironMiner -> ironMiner;
            case builder -> builder;
            case blacksmith -> blacksmith;
            case farmer -> farmer;
            case carrier -> carrier;
            case guard -> guard;
            case spy -> spy;
            case bowman -> bowman;
            case infantry -> infantry;
            case cavalry -> cavalry;
        };
    }

    public KingdomUnitsDto toDto()
    {
        var map = new UnitsMapDto();
        map.setCount(UnitName.goldMiner, goldMiner);
        map.setCount(UnitName.ironMiner, ironMiner);
        map.setCount(UnitName.builder, builder);
        map.setCount(UnitName.blacksmith, blacksmith);
        map.setCount(UnitName.farmer, farmer);
        map.setCount(UnitName.carrier, carrier);
        map.setCount(UnitName.guard, guard);
        map.setCount(UnitName.spy, spy);
        map.setCount(UnitName.bowman, bowman);
        map.setCount(UnitName.infantry, infantry);
        map.setCount(UnitName.cavalry, cavalry);
        return new KingdomUnitsDto(map, new UnitsMapDto());
    }
}
