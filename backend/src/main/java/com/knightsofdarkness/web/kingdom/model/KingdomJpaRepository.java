package com.knightsofdarkness.web.kingdom.model;

import java.util.Optional;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface KingdomJpaRepository extends JpaRepository<KingdomEntity, String>
{

    @Query("SELECT k FROM KingdomEntity k JOIN FETCH k.specialBuildings WHERE k.id = :id")
    Optional<KingdomEntity> findById(Long id);

    /**
     * a single update so the periodic turn never overwrites what players changed in the meantime, and a new version so
     * a player change read before the tick is rejected instead of overwriting the turn
     */
    @Modifying
    @Query("UPDATE KingdomEntity k SET k.resources.turns = k.resources.turns + 1, k.version = k.version + 1 WHERE k.resources.turns < :maxTurns")
    int addTurnBelow(int maxTurns);

}
