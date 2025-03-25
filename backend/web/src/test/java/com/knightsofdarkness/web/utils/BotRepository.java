package com.knightsofdarkness.web.utils;

import java.util.ArrayList;
import java.util.List;

import com.knightsofdarkness.web.bots.IBotRepository;
import com.knightsofdarkness.web.bots.model.BotEntity;

public class BotRepository implements IBotRepository {
    private final List<BotEntity> bots = new ArrayList<>();

    @Override
    public List<BotEntity> findAll()
    {
        return bots;
    }

    @Override
    public BotEntity add(BotEntity bot)
    {
        bots.add(bot);
        return bot;
    }
}
