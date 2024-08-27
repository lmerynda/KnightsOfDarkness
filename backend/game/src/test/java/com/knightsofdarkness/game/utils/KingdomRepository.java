package com.knightsofdarkness.game.utils;

import java.util.ArrayList;
import java.util.List;

import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.game.storage.IKingdomRepository;

public class KingdomRepository implements IKingdomRepository {

    List<Kingdom> kingdoms = new ArrayList<>();

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
