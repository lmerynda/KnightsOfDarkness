package com.knightsofdarkness.common.kingdom;

import java.util.EnumMap;
import java.util.Map;

public class KingdomDetailsDto {
    Map<KingdomDetailName, Integer> details = new EnumMap<>(KingdomDetailName.class);

    public KingdomDetailsDto()
    {
        for (var name : KingdomDetailName.values())
        {
            details.put(name, 0);
        }
    }

    public KingdomDetailsDto(Map<KingdomDetailName, Integer> details)
    {
        this.details = details;
    }

    @SuppressWarnings("java:S107")
    public KingdomDetailsDto(int usedLand)
    {
        details.put(KingdomDetailName.usedLand, usedLand);
    }

    public int getCount(KingdomDetailName name)
    {
        return details.get(name);
    }

    public void setCount(KingdomDetailName name, int count)
    {
        details.put(name, count);
    }

    public Map<KingdomDetailName, Integer> getDetails()
    {
        return details;
    }

    public void setDetail(String key, int value)
    {
        details.put(KingdomDetailName.from(key), value);
    }

    public String toString()
    {
        return details.toString();
    }
}
