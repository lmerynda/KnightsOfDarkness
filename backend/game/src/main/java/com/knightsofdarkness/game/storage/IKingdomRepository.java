package com.knightsofdarkness.game.storage;

import com.knightsofdarkness.game.kingdom.Kingdom;

public interface IKingdomRepository {
    void add(Kingdom kingdom);

    void update(Kingdom kingdom);
}
