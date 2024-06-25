package com.knightsofdarkness.storage.kingdom;

import java.util.Optional;

public class KingdomReadRepository {
    private final KingdomJpaRepository jpaRepository;

    public KingdomReadRepository(KingdomJpaRepository jpaRepository)
    {
        this.jpaRepository = jpaRepository;
    }

    public Optional<KingdomEntity> getKingdomByName(String name)
    {
        return jpaRepository.findById(name);
    }
}
