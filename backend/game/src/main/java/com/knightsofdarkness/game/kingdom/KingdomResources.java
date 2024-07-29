package com.knightsofdarkness.game.kingdom;

import java.util.EnumMap;
import java.util.Map;

public class KingdomResources {
    Map<ResourceName, Integer> resources = new EnumMap<>(ResourceName.class);

    public KingdomResources()
    {
        for (var name : ResourceName.values())
        {
            resources.put(name, 0);
        }
    }

    public KingdomResources(KingdomResources other)
    {
        resources = new EnumMap<>(other.resources);
    }

    public int getCount(ResourceName name)
    {
        return resources.get(name);
    }

    public void addCount(ResourceName name, int count)
    {
        resources.put(name, resources.get(name) + count);
    }

    public void subtractCount(ResourceName name, int count)
    {
        assert resources.get(name) >= count;
        resources.put(name, resources.get(name) - count);
    }

    public void setCount(ResourceName name, int count)
    {
        assert count >= 0;
        resources.put(name, count);
    }

    public String toString()
    {
        return resources.toString();
    }
}
