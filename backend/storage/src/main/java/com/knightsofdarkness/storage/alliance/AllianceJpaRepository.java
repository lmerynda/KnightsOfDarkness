package com.knightsofdarkness.storage.alliance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AllianceJpaRepository extends JpaRepository<AllianceEntity, String> {

}
