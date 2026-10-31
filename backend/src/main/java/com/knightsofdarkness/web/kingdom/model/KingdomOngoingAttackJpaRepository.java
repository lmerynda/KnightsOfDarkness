package com.knightsofdarkness.web.kingdom.model;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KingdomOngoingAttackJpaRepository extends JpaRepository<KingdomOngoingAttackEntity, UUID>
{
    List<KingdomOngoingAttackEntity> findByTargetKingdomName(String targetKingdomName);
}
