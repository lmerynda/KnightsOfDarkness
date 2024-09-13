package com.knightsofdarkness.game.storage;

import java.util.Optional;

import com.knightsofdarkness.game.kingdom.Kingdom;

public interface IKingdomRepository {
    Optional<Kingdom> getKingdomByName(String name);

    void add(Kingdom kingdom);

    void update(Kingdom kingdom);
}
