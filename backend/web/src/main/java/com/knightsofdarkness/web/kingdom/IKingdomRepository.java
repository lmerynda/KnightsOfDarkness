package com.knightsofdarkness.web.kingdom;

import java.util.Optional;

import com.knightsofdarkness.web.kingdom.model.KingdomEntity;

public interface IKingdomRepository {
    Optional<KingdomEntity> getKingdomByName(String name);

    void add(KingdomEntity kingdom);

    void update(KingdomEntity kingdom);
}
