package com.knightsofdarkness.web.kingdom.model;

import java.util.Optional;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface KingdomJpaRepository extends JpaRepository<KingdomEntity, String>
{

    @Query("SELECT k FROM KingdomEntity k JOIN FETCH k.specialBuildings WHERE k.id = :id")
    Optional<KingdomEntity> findById(Long id);

    @Query("SELECT k FROM KingdomEntity k ORDER BY k.resources.land DESC")
    List<KingdomEntity> findTopOrderByLandDesc(Pageable pageable);
}
