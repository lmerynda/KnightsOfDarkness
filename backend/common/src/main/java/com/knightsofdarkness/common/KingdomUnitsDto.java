package com.knightsofdarkness.common;

import com.knightsofdarkness.game.kingdom.KingdomUnits;
import com.knightsofdarkness.game.kingdom.UnitName;

public class KingdomUnitsDto {
    public int goldMiners;
    public int ironMiners;
    public int farmers;
    public int blacksmiths;
    public int builders;
    public int carriers;
    public int guards;
    public int spies;
    public int infantry;
    public int bowmen;
    public int cavalry;

    public KingdomUnitsDto()
    {
    }

    @SuppressWarnings("java:S107")
    public KingdomUnitsDto(int goldMiners, int ironMiners, int farmers, int blacksmiths, int builders, int carriers, int guards, int spies, int infantry, int bowmen, int cavalry)
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

    public KingdomUnits toDomain()
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

    public static KingdomUnitsDto fromDomain(KingdomUnits kingdomUnits)
    {
        return new KingdomUnitsDto(
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

    public String toString()
    {
        return "KingdomUnitsDto{" +
                "goldMiners=" + goldMiners +
                ", ironMiners=" + ironMiners +
                ", farmers=" + farmers +
                ", blacksmiths=" + blacksmiths +
                ", builders=" + builders +
                ", carriers=" + carriers +
                ", guards=" + guards +
                ", spies=" + spies +
                ", infantry=" + infantry +
                ", bowmen=" + bowmen +
                ", cavalry=" + cavalry +
                '}';
    }
}
