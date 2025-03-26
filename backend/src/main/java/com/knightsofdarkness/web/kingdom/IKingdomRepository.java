package com.knightsofdarkness.web.kingdom;

import java.util.Optional;

import com.knightsofdarkness.web.kingdom.model.KingdomEntity;

public interface IKingdomRepository {
    Optional<KingdomEntity> getKingdomByName(String name);

    KingdomEntity add(KingdomEntity kingdom);

    KingdomEntity update(KingdomEntity kingdom);
}
