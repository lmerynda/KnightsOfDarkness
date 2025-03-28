package com.knightsofdarkness.web.bots;

import java.util.List;

import com.knightsofdarkness.web.bots.model.BotEntity;

public interface IBotRepository {
    List<BotEntity> findAllWithKingdoms();

    BotEntity add(BotEntity bot);
}
