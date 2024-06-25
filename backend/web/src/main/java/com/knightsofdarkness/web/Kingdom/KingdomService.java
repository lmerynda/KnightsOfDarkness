package com.knightsofdarkness.web.Kingdom;

import java.util.Optional;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.knightsofdarkness.common.KingdomBuildingsDto;
import com.knightsofdarkness.common.KingdomDto;
import com.knightsofdarkness.common.KingdomUnitsDto;
import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.storage.kingdom.KingdomReadRepository;
import com.knightsofdarkness.storage.kingdom.KingdomRepository;

@Service
public class KingdomService {
    private final Logger log = LoggerFactory.getLogger(KingdomService.class);

    private final GameConfig gameConfig;

    private final KingdomRepository kingdomRepository;
    private final KingdomReadRepository kingdomReadRepository;

    public KingdomService(GameConfig gameConfig, KingdomRepository kingdomRepository, KingdomReadRepository kingdomReadRepository)
    {
        this.gameConfig = gameConfig;
        this.kingdomRepository = kingdomRepository;
        this.kingdomReadRepository = kingdomReadRepository;
    }

    @Transactional
    public KingdomDto createKingdom(KingdomDto kingdom)
    {
        log.info("Creating new kingdom " + kingdom.toString());

        kingdomRepository.add(kingdom.toDomain(gameConfig, new ArrayList<>()));
        return kingdom;
    }

    public Optional<KingdomDto> getKingdomByName(String name)
    {
        log.info("Looking for a kingdom with name " + name);
        return kingdomReadRepository.getKingdomByName(name);
    }

    @Transactional
    public ResponseEntity<Integer> build(String name, KingdomBuildingsDto buildings)
    {
        log.info("[" + name + "] building " + buildings.toString());
        Optional<Kingdom> maybeKingdom = kingdomRepository.getKingdomByName(name);
        if (maybeKingdom.isEmpty())
        {
            return ResponseEntity.notFound().build();
        }

        var kingdom = maybeKingdom.get();

        var howManyWereBuilt = kingdom.build(buildings.toDomain());
        kingdomRepository.update(kingdom);
        return ResponseEntity.ok(howManyWereBuilt);
    }

    @Transactional
    public ResponseEntity<KingdomDto> train(String name, KingdomUnitsDto unitsToTrain)
    {
        log.info("[" + name + "] training " + unitsToTrain.toString());
        Optional<Kingdom> kingdom = kingdomRepository.getKingdomByName(name);
        if (kingdom.isEmpty())
        {
            return ResponseEntity.notFound().build();
        }

        kingdom.get().train(unitsToTrain.toDomain());
        kingdomRepository.update(kingdom.get());

        return ResponseEntity.ok(KingdomDto.fromDomain(kingdom.get()));
    }

    @Transactional
    public ResponseEntity<KingdomDto> passTurn(String name)
    {
        log.info("[" + name + "] passing turn ");
        Optional<Kingdom> kingdom = kingdomRepository.getKingdomByName(name);
        if (kingdom.isEmpty())
        {
            return ResponseEntity.notFound().build();
        }

        // TODO turn report
        boolean isSucessful = kingdom.get().passTurn();
        if (!isSucessful)
        {
            // TODO this action just failed on business logic, we should not return error
            // probably we just just miss part of the domain? PassTurn report?
            return ResponseEntity.notFound().build();
        }

        kingdomRepository.update(kingdom.get());

        return ResponseEntity.ok(KingdomDto.fromDomain(kingdom.get()));
    }
}
