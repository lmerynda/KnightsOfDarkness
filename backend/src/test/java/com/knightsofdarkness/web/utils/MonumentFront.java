package com.knightsofdarkness.web.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.knightsofdarkness.web.kingdom.IMonumentFront;
import com.knightsofdarkness.web.monument.model.MonumentEntity;

public class MonumentFront implements IMonumentFront {
    public final List<MonumentEntity> monuments = new ArrayList<>();
    public final List<String> heard = new ArrayList<>();

    @Override
    public boolean standsAt(String seat)
    {
        return findAtSeatForUpdate(seat).isPresent();
    }

    @Override
    public Optional<MonumentEntity> findAtSeatForUpdate(String seat)
    {
        return monuments.stream().filter(monument -> monument.getSeat().equals(seat)).findFirst();
    }

    @Override
    public void assaultWon(MonumentEntity monument, String attacker)
    {
        heard.add(attacker + " won at " + monument.getAllianceName());
    }

    @Override
    public void assaultRepelled(MonumentEntity monument, String attacker)
    {
        heard.add(attacker + " repelled at " + monument.getAllianceName());
    }
}
