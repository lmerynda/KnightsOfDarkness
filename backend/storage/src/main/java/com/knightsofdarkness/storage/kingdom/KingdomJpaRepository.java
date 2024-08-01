package com.knightsofdarkness.storage.kingdom;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface KingdomJpaRepository extends JpaRepository<KingdomEntity, String> {

    @Query("SELECT k FROM KingdomEntity k JOIN FETCH k.specialBuildings WHERE k.id = :id")
    Optional<KingdomEntity> findById(Long id);

}
