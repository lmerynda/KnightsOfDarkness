package com.knightsofdarkness.web.alliance;

import java.util.Optional;

import java.util.List;
import java.util.UUID;

import com.knightsofdarkness.web.alliance.model.AllianceEntity;
import com.knightsofdarkness.web.alliance.model.AllianceInvitationEntity;

public interface IAllianceRepository {
    Optional<AllianceEntity> getAllianceByName(String name);

    void update(AllianceEntity alliance);

    void create(AllianceEntity alliance);

    List<AllianceEntity> getAlliances();

    AllianceInvitationEntity createInvitation(AllianceInvitationEntity invitation);

    Optional<AllianceInvitationEntity> getInvitationById(UUID id);

    void deleteInvitation(AllianceInvitationEntity invitation);
}
