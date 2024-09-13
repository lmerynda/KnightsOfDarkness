package com.knightsofdarkness.web.kingdom;

import java.util.Optional;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.knightsofdarkness.common.kingdom.KingdomBuildingsDto;
import com.knightsofdarkness.common.kingdom.KingdomDto;
import com.knightsofdarkness.common.kingdom.KingdomSpecialBuildingBuildDto;
import com.knightsofdarkness.common.kingdom.KingdomSpecialBuildingDemolishDto;
import com.knightsofdarkness.common.kingdom.KingdomSpecialBuildingStartDto;
import com.knightsofdarkness.common.kingdom.KingdomTurnReport;
import com.knightsofdarkness.common.kingdom.KingdomUnitsDto;
import com.knightsofdarkness.common.kingdom.LandTransaction;
import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.game.kingdom.KingdomSpecialBuilding;
import com.knightsofdarkness.storage.kingdom.KingdomReadRepository;
import com.knightsofdarkness.storage.kingdom.KingdomRepository;
import com.knightsofdarkness.storage.market.MarketOfferReadRepository;

@Service
public class KingdomService {
    private final Logger log = LoggerFactory.getLogger(KingdomService.class);
    private final KingdomRepository kingdomRepository;
    private final KingdomReadRepository kingdomReadRepository;
    private final MarketOfferReadRepository marketOfferReadRepository;

    public KingdomService(KingdomRepository kingdomRepository, KingdomReadRepository kingdomReadRepository, MarketOfferReadRepository marketOfferReadRepository)
    {
        this.kingdomRepository = kingdomRepository;
        this.kingdomReadRepository = kingdomReadRepository;
        this.marketOfferReadRepository = marketOfferReadRepository;
    }

    @Transactional
    public KingdomDto createKingdom(KingdomDto kingdomDto)
    {
        log.info("Creating new kingdom {}", kingdomDto);

        // TODO new kingdom doesn't have turn reports or special buildings, this is domain level information and should be there
        kingdomDto.lastTurnReport = new KingdomTurnReport();
        kingdomDto.specialBuildings = new ArrayList<>();
        kingdomRepository.add(kingdomDto);
        // TODO I bet the return value should be different
        return kingdomDto;
    }

    public Optional<KingdomDto> getKingdomByName(String name)
    {
        log.info("Looking for a kingdom with name {}", name);
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
    public ResponseEntity<KingdomBuildingsDto> build(String kingdomName, KingdomBuildingsDto buildings)
    {
        log.info("[{}] building {}", kingdomName, buildings);
        Optional<Kingdom> maybeKingdom = kingdomRepository.getKingdomByName(kingdomName);
        if (maybeKingdom.isEmpty())
        {
            return ResponseEntity.notFound().build();
        }

        var kingdom = maybeKingdom.get();

        var buildingsBuilt = kingdom.build(buildings);
        kingdomRepository.update(kingdom);
        return ResponseEntity.ok(buildingsBuilt);
    }

    public ResponseEntity<KingdomBuildingsDto> demolish(String kingdomName, KingdomBuildingsDto buildings)
    {
        log.info("[{}] demolishing {}", kingdomName, buildings);
        Optional<Kingdom> maybeKingdom = kingdomRepository.getKingdomByName(kingdomName);
        if (maybeKingdom.isEmpty())
        {
            return ResponseEntity.notFound().build();
        }

        var kingdom = maybeKingdom.get();

        var buildingsDemolished = kingdom.demolish(buildings);
        kingdomRepository.update(kingdom);
        return ResponseEntity.ok(buildingsDemolished);
    }

    @Transactional
    public ResponseEntity<KingdomUnitsDto> train(String name, KingdomUnitsDto unitsToTrain)
    {
        log.info("[{}] training {}", name, unitsToTrain);
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
        log.info("[{}] passing turn", name);
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

        return ResponseEntity.ok(passedTurnResult.get());
    }

    @Transactional
    public ResponseEntity<LandTransaction> buyLand(String name, int amount)
    {
        log.info("[{}] buying land {}", name, amount);
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
        log.info("[{}] starting special building {}", name, specialBuildingStartDto.name());

        Optional<Kingdom> kingdom = kingdomRepository.getKingdomByName(name);
        if (kingdom.isEmpty())
        {
            return ResponseEntity.notFound().build();
        }

        Optional<KingdomSpecialBuilding> specialBuilding = kingdom.get().startSpecialBuilding(specialBuildingStartDto.name());
        kingdomRepository.update(kingdom.get());
        return ResponseEntity.of(specialBuilding);
    }

    public ResponseEntity<Integer> buildSpecialBuilding(String name, KingdomSpecialBuildingBuildDto specialBuildingBuildDto)
    {
        log.info("[{}] building special building {}", name, specialBuildingBuildDto);
        Optional<Kingdom> maybeKingdom = kingdomRepository.getKingdomByName(name);
        if (maybeKingdom.isEmpty())
        {
            return ResponseEntity.notFound().build();
        }

        var kingdom = maybeKingdom.get();
        Optional<KingdomSpecialBuilding> maybeSpecialBuilding = kingdom.getSpecialBuildings().stream().filter(specialBuilding -> specialBuilding.getId().equals(specialBuildingBuildDto.id())).findFirst();
        if (maybeSpecialBuilding.isEmpty())
        {
            return ResponseEntity.notFound().build();
        }

        var spentPoints = kingdom.buildSpecialBuilding(maybeSpecialBuilding.get(), specialBuildingBuildDto.buildingPoints());
        kingdomRepository.update(kingdom);

        return ResponseEntity.ok(spentPoints);
    }

    public ResponseEntity<Boolean> demolishSpecialBuilding(String name, KingdomSpecialBuildingDemolishDto specialBuildingDemolishDto)
    {
        log.info("[{}] demolishing special building {}", name, specialBuildingDemolishDto);
        Optional<Kingdom> maybeKingdom = kingdomRepository.getKingdomByName(name);
        if (maybeKingdom.isEmpty())
        {
            return ResponseEntity.notFound().build();
        }

        var kingdom = maybeKingdom.get();
        var demolished = kingdom.demolishSpecialBuilding(specialBuildingDemolishDto.id());
        kingdomRepository.update(kingdom);

        return ResponseEntity.ok(demolished);
    }
}
