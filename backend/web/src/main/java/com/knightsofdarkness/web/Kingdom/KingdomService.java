package com.knightsofdarkness.web.Kingdom;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.knightsofdarkness.common.KingdomBuildingsDto;
import com.knightsofdarkness.common.KingdomDto;
import com.knightsofdarkness.common.KingdomSpecialBuildingStartDto;
import com.knightsofdarkness.common.KingdomUnitsDto;
import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.game.kingdom.KingdomSpecialBuilding;
import com.knightsofdarkness.game.kingdom.KingdomTurnReport;
import com.knightsofdarkness.game.kingdom.LandTransaction;
import com.knightsofdarkness.storage.kingdom.KingdomReadRepository;
import com.knightsofdarkness.storage.kingdom.KingdomRepository;
import com.knightsofdarkness.storage.market.MarketOfferReadRepository;

@Service
public class KingdomService {
    private final Logger log = LoggerFactory.getLogger(KingdomService.class);
    private final GameConfig gameConfig;
    private final KingdomRepository kingdomRepository;
    private final KingdomReadRepository kingdomReadRepository;
    private final MarketOfferReadRepository marketOfferReadRepository;

    public KingdomService(GameConfig gameConfig, KingdomRepository kingdomRepository, KingdomReadRepository kingdomReadRepository,  MarketOfferReadRepository marketOfferReadRepository)
    {
        this.gameConfig = gameConfig;
        this.kingdomRepository = kingdomRepository;
        this.kingdomReadRepository = kingdomReadRepository;
        this.marketOfferReadRepository = marketOfferReadRepository;
    }

    @Transactional
    public KingdomDto createKingdom(KingdomDto kingdom)
    {
        log.info("Creating new kingdom " + kingdom.toString());

        kingdomRepository.add(kingdom.toDomain(gameConfig));
        return kingdom;
    }

    public Optional<KingdomDto> getKingdomByName(String name)
    {
        log.info("Looking for a kingdom with name " + name);
        var maybeKingdom = kingdomReadRepository.getKingdomByName(name);
        if (maybeKingdom.isEmpty())
        {
            return Optional.empty();
        }
        var kingdom = maybeKingdom.get();
        var offers = marketOfferReadRepository.findByKingdomName(name);
        kingdom.marketOffers = offers;
        return Optional.of(kingdom);
    }

    @Transactional
    public ResponseEntity<KingdomBuildingsDto> build(String name, KingdomBuildingsDto buildings)
    {
        log.info("[" + name + "] building " + buildings.toString());
        Optional<Kingdom> maybeKingdom = kingdomRepository.getKingdomByName(name);
        if (maybeKingdom.isEmpty())
        {
            return ResponseEntity.notFound().build();
        }

        var kingdom = maybeKingdom.get();

        var buildingsBuilt = kingdom.build(buildings.toDomain());
        kingdomRepository.update(kingdom);
        return ResponseEntity.ok(KingdomBuildingsDto.fromDomain(buildingsBuilt));
    }

    @Transactional
    public ResponseEntity<KingdomUnitsDto> train(String name, KingdomUnitsDto unitsToTrain)
    {
        log.info("[" + name + "] training " + unitsToTrain.toString());
        Optional<Kingdom> kingdom = kingdomRepository.getKingdomByName(name);
        if (kingdom.isEmpty())
        {
            return ResponseEntity.notFound().build();
        }

        var unitsTrained = kingdom.get().train(unitsToTrain.toDomain());
        kingdomRepository.update(kingdom.get());

        return ResponseEntity.ok(KingdomUnitsDto.fromDomain(unitsTrained));
    }

    @Transactional
    public ResponseEntity<KingdomTurnReport> passTurn(String name)
    {
        log.info("[" + name + "] passing turn ");
        Optional<Kingdom> kingdom = kingdomRepository.getKingdomByName(name);
        if (kingdom.isEmpty())
        {
            return ResponseEntity.notFound().build();
        }

        // TODO turn report
        var passedTurnResult = kingdom.get().passTurn();
        if (passedTurnResult.isEmpty())
        {
            // TODO this action just failed on business logic, we should not return error
            // probably we just just miss part of the domain? PassTurn report?
            return ResponseEntity.notFound().build();
        }

        kingdomRepository.update(kingdom.get());
        log.info(passedTurnResult.get().toString());

        return ResponseEntity.ok(passedTurnResult.get());
    }

    @Transactional
    public ResponseEntity<LandTransaction> buyLand(String name, int amount)
    {
        log.info("[" + name + "] buying land");
        Optional<Kingdom> kingdom = kingdomRepository.getKingdomByName(name);
        if (kingdom.isEmpty())
        {
            return ResponseEntity.notFound().build();
        }

        var transaction = kingdom.get().buyLand(amount);
        kingdomRepository.update(kingdom.get());

        return ResponseEntity.ok(transaction);
    }

    @Transactional
    public void addTurnForEveryone()
    {
        log.info("Adding turn for everyone");
        kingdomRepository.getAllKingdoms().forEach(kingdom ->
        {
            kingdom.addTurn();
            kingdomRepository.update(kingdom);
        });
    }

    @Transactional
    public ResponseEntity<KingdomSpecialBuilding> startSpecialBuilding(String name, KingdomSpecialBuildingStartDto specialBuildingStartDto)
    {
        log.info("[{}] starting special building {}", name, specialBuildingStartDto.name);

        Optional<Kingdom> kingdom = kingdomRepository.getKingdomByName(name);
        if (kingdom.isEmpty())
        {
            return ResponseEntity.notFound().build();
        }

        Optional<KingdomSpecialBuilding> specialBuilding = kingdom.get().startSpecialBuilding(specialBuildingStartDto.name);
        kingdomRepository.update(kingdom.get());
        return ResponseEntity.of(specialBuilding);
    }
}
