package com.knightsofdarkness.web.Kingdom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.storage.kingdom.KingdomRepository;

import jakarta.persistence.EntityManager;

@Service
public class KingdomService {
    private final Logger log = LoggerFactory.getLogger(KingdomService.class);

    @Autowired
    private GameConfig gameConfig;

    @Autowired
    private KingdomRepository kingdomRepository;

    @Autowired
    private EntityManager entityManager;

    @Transactional
    public void createKingdom(KingdomDto kingdom)
    {
        log.info("Creating new kingdom" + kingdom.toString());
        kingdomRepository.add(kingdom.toDomain(gameConfig));
    }

    public KingdomDto getKingdomByName(String name)
    {
        log.info("Looking for a kingdom with name " + name);
        var kingdom = kingdomRepository.getKingdomByName(name);
        log.info("Found " + kingdom);
        return KingdomDto.fromDomain(kingdom);
    }
}
