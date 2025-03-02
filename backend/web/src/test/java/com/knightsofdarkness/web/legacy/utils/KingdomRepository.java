package com.knightsofdarkness.game.utils;

import java.util.Optional;

import java.util.ArrayList;
import java.util.List;

import com.knightsofdarkness.web.kingdom.IKingdomRepository;
import com.knightsofdarkness.web.kingdom.legacy.Kingdom;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;

public class KingdomRepository implements IKingdomRepository {

    List<Kingdom> kingdoms = new ArrayList<>();

    @Override
    public Optional<KingdomEntity> getKingdomByName(String name)
    {
        return kingdoms.stream().filter(kingdom -> kingdom.getName().equals(name)).findFirst();
    }

    @Override
    public void update(Kingdom kingdom)
    {
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
    public void add(Kingdom kingdom)
    {
        kingdoms.add(kingdom);
    }
}
