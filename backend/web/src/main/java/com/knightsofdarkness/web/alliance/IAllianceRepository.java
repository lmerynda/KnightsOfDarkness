package com.knightsofdarkness.web.alliance;

import java.util.Optional;

import java.util.List;

import com.knightsofdarkness.common.alliance.CreateAllianceDto;
import com.knightsofdarkness.web.alliance.model.AllianceEntity;

public interface IAllianceRepository {
    Optional<AllianceEntity> getAllianceByName(String name);

    void add(CreateAllianceDto alliance, String emperor);

    void update(AllianceEntity alliance);

    List<AllianceEntity> getAlliances();
}
