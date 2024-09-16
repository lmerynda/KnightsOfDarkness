package com.knightsofdarkness.storage.kingdom;

import com.knightsofdarkness.common.kingdom.KingdomUnitsDto;
import com.knightsofdarkness.common.kingdom.UnitName;
import com.knightsofdarkness.game.kingdom.KingdomUnits;

import jakarta.persistence.Embeddable;

@Embeddable
public class KingdomUnitsEntity {
    int goldMiner;
    int ironMiner;
    int farmer;
    int blacksmith;
    int builder;
    int carrier;
    int guard;
    int spy;
    int infantry;
    int bowman;
    int cavalry;

    public KingdomUnitsEntity()
    {
    }

    @SuppressWarnings("java:S107")
    public KingdomUnitsEntity(int goldMiner, int ironMiner, int farmer, int blacksmith, int builder, int carrier, int guard, int spy, int infantry, int bowman, int cavalry)
    {
        this.goldMiner = goldMiner;
        this.ironMiner = ironMiner;
        this.farmer = farmer;
        this.blacksmith = blacksmith;
        this.builder = builder;
        this.carrier = carrier;
        this.guard = guard;
        this.spy = spy;
        this.infantry = infantry;
        this.bowman = bowman;
        this.cavalry = cavalry;
    }

    public KingdomUnits toDomainModel()
    {
        var kingdomUnit = new KingdomUnits();
        kingdomUnit.setCount(UnitName.goldMiner, goldMiner);
        kingdomUnit.setCount(UnitName.ironMiner, ironMiner);
        kingdomUnit.setCount(UnitName.farmer, farmer);
        kingdomUnit.setCount(UnitName.blacksmith, blacksmith);
        kingdomUnit.setCount(UnitName.builder, builder);
        kingdomUnit.setCount(UnitName.carrier, carrier);
        kingdomUnit.setCount(UnitName.guard, guard);
        kingdomUnit.setCount(UnitName.spy, spy);
        kingdomUnit.setCount(UnitName.infantry, infantry);
        kingdomUnit.setCount(UnitName.bowman, bowman);
        kingdomUnit.setCount(UnitName.cavalry, cavalry);
        return kingdomUnit;
    }

    public KingdomUnitsDto toDto()
    {
        return new KingdomUnitsDto(goldMiner, ironMiner, farmer, blacksmith, builder, carrier, guard, spy, infantry, bowman, cavalry);
    }

    public static KingdomUnitsEntity fromDomainModel(KingdomUnits kingdomUnits)
    {
        return new KingdomUnitsEntity(
                kingdomUnits.getCount(UnitName.goldMiner),
                kingdomUnits.getCount(UnitName.ironMiner),
                kingdomUnits.getCount(UnitName.farmer),
                kingdomUnits.getCount(UnitName.blacksmith),
                kingdomUnits.getCount(UnitName.builder),
                kingdomUnits.getCount(UnitName.carrier),
                kingdomUnits.getCount(UnitName.guard),
                kingdomUnits.getCount(UnitName.spy),
                kingdomUnits.getCount(UnitName.infantry),
                kingdomUnits.getCount(UnitName.bowman),
                kingdomUnits.getCount(UnitName.cavalry));
    }

    public static KingdomUnitsEntity fromDto(KingdomUnitsDto dto)
    {
        return new KingdomUnitsEntity(
                dto.getCount(UnitName.goldMiner),
                dto.getCount(UnitName.ironMiner),
                dto.getCount(UnitName.farmer),
                dto.getCount(UnitName.blacksmith),
                dto.getCount(UnitName.builder),
                dto.getCount(UnitName.carrier),
                dto.getCount(UnitName.guard),
                dto.getCount(UnitName.spy),
                dto.getCount(UnitName.infantry),
                dto.getCount(UnitName.bowman),
                dto.getCount(UnitName.cavalry));
    }

}
