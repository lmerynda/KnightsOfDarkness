package com.knightsofdarkness.web.kingdom.model;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.knightsofdarkness.web.common.kingdom.KingdomDto;

@Repository
public class KingdomReadRepository {
    private final KingdomJpaRepository jpaRepository;

    public KingdomReadRepository(KingdomJpaRepository jpaRepository)
    {
        this.jpaRepository = jpaRepository;
    }

    public Optional<KingdomDto> getKingdomByName(String name)
    {
        return jpaRepository.findById(name).map(KingdomEntity::toDto);
    }
}
