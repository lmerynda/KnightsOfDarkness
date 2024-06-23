package com.knightsofdarkness.storage.kingdom;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KingdomJpaRepository extends JpaRepository<KingdomEntity, String> {

}
