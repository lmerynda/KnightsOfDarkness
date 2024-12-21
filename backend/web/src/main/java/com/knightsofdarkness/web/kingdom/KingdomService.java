package com.knightsofdarkness.web.kingdom;

import java.util.Optional;

import java.util.ArrayList;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.knightsofdarkness.common.kingdom.KingdomBuildingsActionResult;
import com.knightsofdarkness.common.kingdom.KingdomBuildingsDto;
import com.knightsofdarkness.common.kingdom.KingdomDto;
import com.knightsofdarkness.common.kingdom.KingdomPassTurnActionResult;
import com.knightsofdarkness.common.kingdom.KingdomSpecialBuildingBuildDto;
import com.knightsofdarkness.common.kingdom.KingdomSpecialBuildingDemolishDto;
import com.knightsofdarkness.common.kingdom.KingdomSpecialBuildingStartDto;
import com.knightsofdarkness.common.kingdom.KingdomTurnReport;
import com.knightsofdarkness.common.kingdom.KingdomUnitsActionResult;
import com.knightsofdarkness.common.kingdom.LandTransaction;
import com.knightsofdarkness.common.kingdom.SendAttackDto;
import com.knightsofdarkness.common.kingdom.SendAttackResult;
import com.knightsofdarkness.common.kingdom.SendCarriersDto;
import com.knightsofdarkness.common.kingdom.SendCarriersResult;
import com.knightsofdarkness.common.kingdom.UnitsMapDto;
import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.game.interactions.IKingdomInteractor;
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
    private final GameConfig gameConfig;
    private final IKingdomInteractor kingdomInteractor;

    public KingdomService(KingdomRepository kingdomRepository, KingdomReadRepository kingdomReadRepository, MarketOfferReadRepository marketOfferReadRepository, GameConfig gameConfig, IKingdomInteractor kingdomInteractor)
    {
        this.kingdomRepository = kingdomRepository;
        this.kingdomReadRepository = kingdomReadRepository;
        this.marketOfferReadRepository = marketOfferReadRepository;
        this.gameConfig = gameConfig;
        this.kingdomInteractor = kingdomInteractor;
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

    @Transactional
    public KingdomDto createKingdom(String name)
    {
        log.info("Creating new kingdom with name {}", name);

        var startConfiguration = gameConfig.kingdomStartConfiguration();
        var kingdomDto = new KingdomDto();
        kingdomDto.name = name;
        kingdomDto.resources = startConfiguration.resources().toDto();
        kingdomDto.units = startConfiguration.units().toDto();
        kingdomDto.buildings = startConfiguration.buildings().toDto();
        kingdomDto.lastTurnReport = new KingdomTurnReport();
        kingdomDto.specialBuildings = new ArrayList<>();

        kingdomRepository.add(kingdomDto);
        // TODO I bet the return value should be different
        return kingdomDto;
    }

    public Optional<KingdomDto> getKingdomByName(String name)
    {
        // log.info("Looking for a kingdom with name {}", name);
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
    public ResponseEntity<KingdomBuildingsActionResult> build(String kingdomName, KingdomBuildingsDto buildings)
    {
        log.info("[{}] buildings to build {}", kingdomName, buildings);
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

    @Transactional
    public ResponseEntity<KingdomBuildingsActionResult> demolish(String kingdomName, KingdomBuildingsDto buildings)
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
    public ResponseEntity<KingdomUnitsActionResult> train(String name, UnitsMapDto unitsToTrain)
    {
        log.info("[{}] training {}", name, unitsToTrain);
        Optional<Kingdom> kingdom = kingdomRepository.getKingdomByName(name);
        if (kingdom.isEmpty())
        {
            return ResponseEntity.notFound().build();
        }

        var unitsTrained = kingdom.get().train(unitsToTrain);
        kingdomRepository.update(kingdom.get());

        return ResponseEntity.ok(unitsTrained);
    }

    @Transactional
    public ResponseEntity<KingdomUnitsActionResult> fireUnits(String name, UnitsMapDto unitsToFire)
    {
        log.info("[{}] firing {}", name, unitsToFire);
        Optional<Kingdom> kingdom = kingdomRepository.getKingdomByName(name);
        if (kingdom.isEmpty())
        {
            return ResponseEntity.notFound().build();
        }

        var unitsFired = kingdom.get().fireUnits(unitsToFire);
        kingdomRepository.update(kingdom.get());

        return ResponseEntity.ok(unitsFired);
    }

    @Transactional
    public ResponseEntity<KingdomPassTurnActionResult> passTurn(String name, int weaponsProductionPercentage)
    {
        log.info("[{}] passing turn", name);
        Optional<Kingdom> kingdom = kingdomRepository.getKingdomByName(name);
        if (kingdom.isEmpty())
        {
            return ResponseEntity.notFound().build();
        }

        var passedTurnResult = kingdom.get().passTurn(kingdomInteractor, weaponsProductionPercentage);
        if (passedTurnResult.success())
        {
            kingdomRepository.update(kingdom.get());
        }

        return ResponseEntity.ok(passedTurnResult);
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

    @Transactional
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

    @Transactional
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

    @Transactional
    public ResponseEntity<SendCarriersResult> sendCarriers(String kingdomName, SendCarriersDto sendCarriersDto)
    {
        log.info("[{}] sending carriers {}", kingdomName, sendCarriersDto);
        Optional<Kingdom> maybeKingdom = kingdomRepository.getKingdomByName(kingdomName);
        if (maybeKingdom.isEmpty())
        {
            return ResponseEntity.notFound().build();
        }
        var kingdom = maybeKingdom.get();

        // TODO no need to fetch entire kingdom, we only want to check if it exists
        Optional<Kingdom> maybeDestinationKingdom = kingdomRepository.getKingdomByName(sendCarriersDto.destinationKingdomName());
        if (maybeDestinationKingdom.isEmpty())
        {
            return ResponseEntity.ok(SendCarriersResult.failure("Destination kingdom does not exist"));
        }

        SendCarriersResult result = kingdom.sendCarriers(sendCarriersDto);
        if (result.success())
        {
            kingdomRepository.update(kingdom);
        }

        return ResponseEntity.ok(result);
    }

    @Transactional
    public ResponseEntity<Boolean> withdrawCarriers(String kingdomName, UUID carriersOnTheMoveId)
    {
        log.info("[{}] withdrawing carriers {}", kingdomName, carriersOnTheMoveId);
        Optional<Kingdom> maybeKingdom = kingdomRepository.getKingdomByName(kingdomName);
        if (maybeKingdom.isEmpty())
        {
            return ResponseEntity.notFound().build();
        }
        var kingdom = maybeKingdom.get();
        var carriersOnTheMove = kingdom.getCarriersOnTheMove().stream().filter(c -> c.getId().equals(carriersOnTheMoveId)).findFirst();
        if (carriersOnTheMove.isEmpty())
        {
            return ResponseEntity.notFound().build();
        }

        kingdom.withdrawCarriers(carriersOnTheMove.get());
        kingdomRepository.update(kingdom);

        return ResponseEntity.ok(true);
    }

    @Transactional
    public ResponseEntity<SendAttackResult> sendAttack(String kingdomName, SendAttackDto sendAttackDto)
    {
        log.info("[{}] sending attack {}", kingdomName, sendAttackDto);
        Optional<Kingdom> maybeKingdom = kingdomRepository.getKingdomByName(kingdomName);
        if (maybeKingdom.isEmpty())
        {
            return ResponseEntity.notFound().build();
        }
        var kingdom = maybeKingdom.get();

        // TODO no need to fetch entire kingdom, we only want to check if it exists
        Optional<Kingdom> maybeDestinationKingdom = kingdomRepository.getKingdomByName(sendAttackDto.destinationKingdomName());
        if (maybeDestinationKingdom.isEmpty())
        {
            return ResponseEntity.ok(SendAttackResult.failure("Destination kingdom does not exist"));
        }

        SendAttackResult result = kingdom.sendAttack(sendAttackDto);
        if (result.success())
        {
            kingdomRepository.update(kingdom);
        }

        return ResponseEntity.ok(result);
    }

    @Transactional
    public ResponseEntity<Boolean> withdrawAttack(String kingdomName, UUID attackId)
    {
        log.info("[{}] withdrawing attack {}", kingdomName, attackId);
        Optional<Kingdom> maybeKingdom = kingdomRepository.getKingdomByName(kingdomName);
        if (maybeKingdom.isEmpty())
        {
            return ResponseEntity.notFound().build();
        }
        var kingdom = maybeKingdom.get();
        var ongoingAttacks = kingdom.getOngoingAttacks().stream().filter(c -> c.getId().equals(attackId)).findFirst();
        if (ongoingAttacks.isEmpty())
        {
            return ResponseEntity.notFound().build();
        }

        kingdom.withdrawAttack(ongoingAttacks.get());
        kingdomRepository.update(kingdom);

        return ResponseEntity.ok(true);
    }
}
