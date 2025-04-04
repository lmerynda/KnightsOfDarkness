package com.knightsofdarkness.web.alliance.model;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AllianceInvitationJpaRepository extends JpaRepository<AllianceInvitationEntity, UUID> {

}
