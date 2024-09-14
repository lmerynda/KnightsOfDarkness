package com.knightsofdarkness.common.kingdom;

import java.util.EnumMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

public class KingdomDetailsDto {
    Map<KingdomDetailName, Integer> details = new EnumMap<>(KingdomDetailName.class);

    public KingdomDetailsDto()
    {
        for (var name : KingdomDetailName.values())
        {
            details.put(name, 0);
        }
    }

    public int getCount(KingdomDetailName name)
    {
        return details.get(name);
    }

    public void setCount(KingdomDetailName name, int count)
    {
        details.put(name, count);
    }

    @SuppressWarnings("java:S107")
    public KingdomDetailsDto(int usedLand)
    {
        details.put(KingdomDetailName.usedLand, usedLand);
    }

    @JsonAnyGetter
    public Map<KingdomDetailName, Integer> getDetails()
    {
        return details;
    }

    @JsonAnySetter
    public void setDetail(String key, int value)
    {
        details.put(KingdomDetailName.from(key), value);
    }

    public String toString()
    {
        return details.toString();
    }
}
