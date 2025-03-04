package com.knightsofdarkness.web.utils;

import java.util.Optional;

import java.util.ArrayList;
import java.util.List;

import com.knightsofdarkness.web.kingdom.IKingdomRepository;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;

public class KingdomRepository implements IKingdomRepository {

    List<KingdomEntity> kingdoms = new ArrayList<>();

    @Override
    public Optional<KingdomEntity> getKingdomByName(String name)
    {
        return kingdoms.stream().filter(kingdom -> kingdom.getName().equals(name)).findFirst();
    }

    @Override
    public void update(KingdomEntity kingdom)
    {
        kingdom.getResources().syncResources();
        kingdom.getBuildings().syncBuildings();
        kingdom.getUnits().syncUnits();
        for (var savedKingdom : kingdoms)
        {
            if (savedKingdom.getName().equals(kingdom.getName()))
            {
                kingdoms.remove(savedKingdom);
                kingdoms.add(kingdom);

                return;
            }
        }

        throw new IllegalArgumentException("Kingdom not found");
    }

    @Override
    public void add(KingdomEntity kingdom)
    {
        kingdom.getResources().syncResources();
        kingdom.getBuildings().syncBuildings();
        kingdom.getUnits().syncUnits();
        kingdoms.add(kingdom);
    }
}
