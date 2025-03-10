package com.knightsofdarkness.web.alliance;

import java.util.Optional;

import java.util.List;

import com.knightsofdarkness.web.alliance.model.AllianceEntity;

public interface IAllianceRepository {
    Optional<AllianceEntity> getAllianceByName(String name);

    void update(AllianceEntity alliance);

    void create(AllianceEntity alliance);

    List<AllianceEntity> getAlliances();
}
