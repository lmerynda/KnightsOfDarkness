package com.knightsofdarkness.storage.kingdom;

import java.util.EnumMap;
import java.util.Map;

import com.knightsofdarkness.common.kingdom.KingdomUnitsDto;
import com.knightsofdarkness.common.kingdom.UnitName;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Transient;

@Embeddable
@Access(AccessType.FIELD)
public class KingdomUnitsEntity {
    int availableGoldMiner;
    int availableIronMiner;
    int availableFarmer;
    int availableBlacksmith;
    int availableBuilder;
    int availableCarrier;
    int availableGuard;
    int availableSpy;
    int availableInfantry;
    int availableBowman;
    int availableCavalry;

    @Transient
    EnumMap<UnitName, Integer> availableUnits = new EnumMap<>(UnitName.class);

    int mobileGoldMiner;
    int mobileIronMiner;
    int mobileFarmer;
    int mobileBlacksmith;
    int mobileBuilder;
    int mobileCarrier;
    int mobileGuard;
    int mobileSpy;
    int mobileInfantry;
    int mobileBowman;
    int mobileCavalry;

    @Transient
    EnumMap<UnitName, Integer> mobileUnits = new EnumMap<>(UnitName.class);

    public KingdomUnitsEntity()
    {
    }

    public KingdomUnitsEntity(Map<UnitName, Integer> availableUnitsMap, Map<UnitName, Integer> mobileUnitsMap)
    {
        availableUnits.putAll(availableUnitsMap);
        mobileUnits.putAll(mobileUnitsMap);
    }

    public KingdomUnitsDto toDto()
    {
        return new KingdomUnitsDto(availableUnits, mobileUnits);
    }

    public void loadMap(Map<UnitName, Integer> availableUnitsMap, Map<UnitName, Integer> mobileUnitsMap)
    {
        availableGoldMiner = availableUnitsMap.get(UnitName.goldMiner);
        availableIronMiner = availableUnitsMap.get(UnitName.ironMiner);
        availableFarmer = availableUnitsMap.get(UnitName.farmer);
        availableBlacksmith = availableUnitsMap.get(UnitName.blacksmith);
        availableBuilder = availableUnitsMap.get(UnitName.builder);
        availableCarrier = availableUnitsMap.get(UnitName.carrier);
        availableGuard = availableUnitsMap.get(UnitName.guard);
        availableSpy = availableUnitsMap.get(UnitName.spy);
        availableInfantry = availableUnitsMap.get(UnitName.infantry);
        availableBowman = availableUnitsMap.get(UnitName.bowman);
        availableCavalry = availableUnitsMap.get(UnitName.cavalry);

        mobileGoldMiner = mobileUnitsMap.get(UnitName.goldMiner);
        mobileIronMiner = mobileUnitsMap.get(UnitName.ironMiner);
        mobileFarmer = mobileUnitsMap.get(UnitName.farmer);
        mobileBlacksmith = mobileUnitsMap.get(UnitName.blacksmith);
        mobileBuilder = mobileUnitsMap.get(UnitName.builder);
        mobileCarrier = mobileUnitsMap.get(UnitName.carrier);
        mobileGuard = mobileUnitsMap.get(UnitName.guard);
        mobileSpy = mobileUnitsMap.get(UnitName.spy);
        mobileInfantry = mobileUnitsMap.get(UnitName.infantry);
        mobileBowman = mobileUnitsMap.get(UnitName.bowman);
        mobileCavalry = mobileUnitsMap.get(UnitName.cavalry);
    }

    public EnumMap<UnitName, Integer> availableToEnumMap()
    {
        var map = new EnumMap<UnitName, Integer>(UnitName.class);
        map.put(UnitName.goldMiner, availableGoldMiner);
        map.put(UnitName.ironMiner, availableIronMiner);
        map.put(UnitName.farmer, availableFarmer);
        map.put(UnitName.blacksmith, availableBlacksmith);
        map.put(UnitName.builder, availableBuilder);
        map.put(UnitName.carrier, availableCarrier);
        map.put(UnitName.guard, availableGuard);
        map.put(UnitName.spy, availableSpy);
        map.put(UnitName.infantry, availableInfantry);
        map.put(UnitName.bowman, availableBowman);
        map.put(UnitName.cavalry, availableCavalry);
        return map;
    }

    public EnumMap<UnitName, Integer> mobileToEnumMap()
    {
        var map = new EnumMap<UnitName, Integer>(UnitName.class);
        map.put(UnitName.goldMiner, mobileGoldMiner);
        map.put(UnitName.ironMiner, mobileIronMiner);
        map.put(UnitName.farmer, mobileFarmer);
        map.put(UnitName.blacksmith, mobileBlacksmith);
        map.put(UnitName.builder, mobileBuilder);
        map.put(UnitName.carrier, mobileCarrier);
        map.put(UnitName.guard, mobileGuard);
        map.put(UnitName.spy, mobileSpy);
        map.put(UnitName.infantry, mobileInfantry);
        map.put(UnitName.bowman, mobileBowman);
        map.put(UnitName.cavalry, mobileCavalry);
        return map;
    }

    public void syncUnits()
    {
        loadMap(availableUnits, mobileUnits);
    }

    @PostLoad
    public void loadUnits()
    {
        availableUnits = availableToEnumMap();
        mobileUnits = mobileToEnumMap();
    }
}
