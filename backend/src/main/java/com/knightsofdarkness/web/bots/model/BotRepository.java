package com.knightsofdarkness.web.bots.model;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.knightsofdarkness.web.bots.IBotRepository;

@Repository
public class BotRepository implements IBotRepository {
    private final BotJpaRepository botJpaRepository;

    public BotRepository(BotJpaRepository botJpaRepository)
    {
        this.botJpaRepository = botJpaRepository;
    }

    public List<BotEntity> findAllWithKingdoms()
    {
        return botJpaRepository.findAllWithKingdoms();
    }

    @Override
    public BotEntity add(BotEntity bot)
    {
        return botJpaRepository.save(bot);
    }
}
