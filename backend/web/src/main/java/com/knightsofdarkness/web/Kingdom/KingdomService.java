package com.knightsofdarkness.web.Kingdom;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.game.market.IMarket;
import com.knightsofdarkness.storage.kingdom.KingdomRepository;

import jakarta.persistence.EntityManager;

@Service
public class KingdomService {
    private final Logger log = LoggerFactory.getLogger(KingdomService.class);

    @Autowired
    private GameConfig gameConfig;

    @Autowired
    private IMarket market;

    @Autowired
    private KingdomRepository kingdomRepository;

    @Autowired
    private EntityManager entityManager;

    @Transactional
    public KingdomDto createKingdom(KingdomDto kingdom)
    {
        log.info("Creating new kingdom " + kingdom.toString());

        var createdKingdom = kingdomRepository.add(kingdom.toDomain(gameConfig, market));
        return KingdomDto.fromDomain(createdKingdom);
    }

    public Optional<KingdomDto> getKingdomByName(String name)
    {
        log.info("Looking for a kingdom with name " + name);
        Optional<Kingdom> kingdom = kingdomRepository.getKingdomByName(name);
        return kingdom.isEmpty() ? Optional.empty() : Optional.of(KingdomDto.fromDomain(kingdom.get()));
    }

    @Transactional
    public ResponseEntity<KingdomDto> build(String name, KingdomBuildingsDto buildings) {
        log.info("[" + name + "] building " + buildings.toString());
        Optional<Kingdom> kingdom = kingdomRepository.getKingdomByName(name);
        if (kingdom.isEmpty())
        {
            return ResponseEntity.notFound().build();
        }

        kingdom.get().build(buildings.toDomain());
        kingdomRepository.update(kingdom.get());
        return ResponseEntity.ok(KingdomDto.fromDomain(kingdom.get()));
    }
}
