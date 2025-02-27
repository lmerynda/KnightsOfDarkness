package com.knightsofdarkness.web.kingdom.model;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.game.storage.IKingdomRepository;

@Repository
public class LegacyKingdomRepository implements IKingdomRepository {

    @Override
    public Optional<Kingdom> getKingdomByName(String name)
    {
        return Optional.empty();
    }

    @Override
    public void add(Kingdom kingdom)
    {
        // XXX Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'add'");
    }

    @Override
    public void update(Kingdom kingdom)
    {
        // XXX Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

}
