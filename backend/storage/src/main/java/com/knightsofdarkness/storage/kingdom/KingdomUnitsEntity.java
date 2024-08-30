package com.knightsofdarkness.storage.kingdom;

import com.knightsofdarkness.common.KingdomUnitsDto;
import com.knightsofdarkness.game.kingdom.KingdomUnits;
import com.knightsofdarkness.game.kingdom.UnitName;

import jakarta.persistence.Embeddable;

@Embeddable
public class KingdomUnitsEntity {
    int goldMiners;
    int ironMiners;
    int farmers;
    int blacksmiths;
    int builders;
    int carriers;
    int guards;
    int spies;
    int infantry;
    int bowmen;
    int cavalry;

    public KingdomUnitsEntity()
    {
    }

    @SuppressWarnings("java:S107")
    public KingdomUnitsEntity(int goldMiners, int ironMiners, int farmers, int blacksmiths, int builders, int carriers, int guards, int spies, int infantry, int bowmen, int cavalry)
    {
        this.goldMiners = goldMiners;
        this.ironMiners = ironMiners;
        this.farmers = farmers;
        this.blacksmiths = blacksmiths;
        this.builders = builders;
        this.carriers = carriers;
        this.guards = guards;
        this.spies = spies;
        this.infantry = infantry;
        this.bowmen = bowmen;
        this.cavalry = cavalry;
    }

    public KingdomUnits toDomainModel()
    {
        var kingdomUnits = new KingdomUnits();
        kingdomUnits.setCount(UnitName.goldMiner, goldMiners);
        kingdomUnits.setCount(UnitName.ironMiner, ironMiners);
        kingdomUnits.setCount(UnitName.farmer, farmers);
        kingdomUnits.setCount(UnitName.blacksmith, blacksmiths);
        kingdomUnits.setCount(UnitName.builder, builders);
        kingdomUnits.setCount(UnitName.carrier, carriers);
        kingdomUnits.setCount(UnitName.guard, guards);
        kingdomUnits.setCount(UnitName.spy, spies);
        kingdomUnits.setCount(UnitName.infantry, infantry);
        kingdomUnits.setCount(UnitName.bowmen, bowmen);
        kingdomUnits.setCount(UnitName.cavalry, cavalry);
        return kingdomUnits;
    }

    public KingdomUnitsDto toDto()
    {
        return new KingdomUnitsDto(goldMiners, ironMiners, farmers, blacksmiths, builders, carriers, guards, spies, infantry, bowmen, cavalry);
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
                kingdomUnits.getCount(UnitName.bowmen),
                kingdomUnits.getCount(UnitName.cavalry));
    }

}
