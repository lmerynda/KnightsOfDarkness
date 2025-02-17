package com.knightsofdarkness.game.storage;

import java.util.Optional;

import java.util.List;

import com.knightsofdarkness.common.alliance.CreateAllianceDto;
import com.knightsofdarkness.game.alliance.Alliance;

public interface IAllianceRepository {
    Optional<Alliance> getAllianceByName(String name);

    void add(CreateAllianceDto alliance, String emperor);

    void update(Alliance alliance);

    List<Alliance> getAlliances();
}
