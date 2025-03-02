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
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.kingdom.model.KingdomBuildAction;
import com.knightsofdarkness.web.kingdom.model.KingdomCarriersAction;
import com.knightsofdarkness.web.kingdom.model.KingdomDetailsProvider;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;
import com.knightsofdarkness.web.kingdom.model.KingdomMilitaryAction;
import com.knightsofdarkness.web.kingdom.model.KingdomOtherAction;
import com.knightsofdarkness.web.kingdom.model.KingdomReadRepository;
import com.knightsofdarkness.web.kingdom.model.KingdomRepository;
import com.knightsofdarkness.web.kingdom.model.KingdomSpecialBuildingAction;
import com.knightsofdarkness.web.kingdom.model.KingdomSpecialBuildingEntity;
import com.knightsofdarkness.web.kingdom.model.KingdomTrainAction;
import com.knightsofdarkness.web.kingdom.model.KingdomTurnAction;
import com.knightsofdarkness.web.market.model.MarketOfferReadRepository;

@Service
public class KingdomService {
    private final Logger log = LoggerFactory.getLogger(KingdomService.class);
    private final KingdomRepository kingdomRepository;
    private final KingdomReadRepository kingdomReadRepository;
    private final MarketOfferReadRepository marketOfferReadRepository;
    private final GameConfig gameConfig;
    private final IKingdomInteractor kingdomInteractor;
    private final KingdomDetailsProvider kingdomDetailsProvider;

    public KingdomService(KingdomRepository kingdomRepository, KingdomReadRepository kingdomReadRepository, MarketOfferReadRepository marketOfferReadRepository, GameConfig gameConfig, IKingdomInteractor kingdomInteractor,
            KingdomDetailsProvider kingdomDetailsProvider)
    {
        this.kingdomRepository = kingdomRepository;
        this.kingdomReadRepository = kingdomReadRepository;
        this.marketOfferReadRepository = marketOfferReadRepository;
        this.gameConfig = gameConfig;
        this.kingdomInteractor = kingdomInteractor;
        this.kingdomDetailsProvider = kingdomDetailsProvider;
    }

    @Transactional
    public KingdomDto createKingdom(KingdomDto kingdomDto)
    {
        log.info("Creating new kingdom {}", kingdomDto);

        // TODO new kingdom doesn't have turn reports or special buildings, this is domain level information and should be there
        kingdomDto.lastTurnReport = new KingdomTurnReport();
        kingdomDto.specialBuildings = new ArrayList<>();
        var kingdom = KingdomEntity.fromDto(kingdomDto);
        kingdomRepository.add(kingdom);
        // TODO I bet the return value should be different
        return kingdomDto;
    }

    // @Transactional
    // public KingdomEntity createKingdom(String name)
    // {
    // log.info("Creating new kingdom with name {}", name);

    // var startConfiguration = gameConfig.kingdomStartConfiguration();
    // var kingdom = new KingdomEntity(
    // name,
    // startConfiguration.resources().toMap());
    // // kingdomDto.units = startConfiguration.units().toDto();
    // // kingdomDto.buildings = startConfiguration.buildings().toDto();
    // // kingdomDto.lastTurnReport = new KingdomTurnReport();
    // // kingdomDto.specialBuildings = new ArrayList<>();

    // kingdomRepository.add(kingdom);
    // // TODO I bet the return value should be different
    // return kingdom;
    // }

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
        Optional<KingdomEntity> maybeKingdom = kingdomRepository.getKingdomByName(kingdomName);
        if (maybeKingdom.isEmpty())
        {
            return ResponseEntity.notFound().build();
        }

        var kingdom = maybeKingdom.get();

        var buildAction = new KingdomBuildAction(kingdom, gameConfig);
        var buildingsBuilt = buildAction.build(buildings);
        kingdomRepository.update(kingdom);
        return ResponseEntity.ok(buildingsBuilt);
    }

    @Transactional
    public ResponseEntity<KingdomBuildingsActionResult> demolish(String kingdomName, KingdomBuildingsDto buildings)
    {
        log.info("[{}] demolishing {}", kingdomName, buildings);
        Optional<KingdomEntity> maybeKingdom = kingdomRepository.getKingdomByName(kingdomName);
        if (maybeKingdom.isEmpty())
        {
            return ResponseEntity.notFound().build();
        }

        var kingdom = maybeKingdom.get();

        var buildAction = new KingdomBuildAction(kingdom, gameConfig);
        var buildingsDemolished = buildAction.demolish(buildings);
        kingdomRepository.update(kingdom);
        return ResponseEntity.ok(buildingsDemolished);
    }

    @Transactional
    public ResponseEntity<KingdomUnitsActionResult> train(String name, UnitsMapDto unitsToTrain)
    {
        log.info("[{}] training {}", name, unitsToTrain);
        Optional<KingdomEntity> maybeKingdom = kingdomRepository.getKingdomByName(name);
        if (maybeKingdom.isEmpty())
        {
            return ResponseEntity.notFound().build();
        }

        var kingdom = maybeKingdom.get();
        var action = new KingdomTrainAction(kingdom, gameConfig);
        var unitsTrained = action.train(unitsToTrain);
        kingdomRepository.update(kingdom);

        return ResponseEntity.ok(unitsTrained);
    }

    @Transactional
    public ResponseEntity<KingdomUnitsActionResult> fireUnits(String name, UnitsMapDto unitsToFire)
    {
        log.info("[{}] firing {}", name, unitsToFire);
        Optional<KingdomEntity> maybeKingdom = kingdomRepository.getKingdomByName(name);
        if (maybeKingdom.isEmpty())
        {
            return ResponseEntity.notFound().build();
        }

        var kingdom = maybeKingdom.get();
        var action = new KingdomTrainAction(kingdom, gameConfig);
        var unitsFired = action.fireUnits(unitsToFire);
        kingdomRepository.update(kingdom);

        return ResponseEntity.ok(unitsFired);
    }

    @Transactional
    public ResponseEntity<KingdomPassTurnActionResult> passTurn(String name, int weaponsProductionPercentage)
    {
        log.info("[{}] passing turn", name);
        Optional<KingdomEntity> maybeKingdom = kingdomRepository.getKingdomByName(name);
        if (maybeKingdom.isEmpty())
        {
            return ResponseEntity.notFound().build();
        }

        var kingdom = maybeKingdom.get();
        var action = new KingdomTurnAction(kingdom, kingdomInteractor, gameConfig, kingdomDetailsProvider);
        var result = action.passTurn(weaponsProductionPercentage);

        if (result.success())
        {
            kingdomRepository.update(kingdom);
        }

        return ResponseEntity.ok(result);
    }

    @Transactional
    public ResponseEntity<LandTransaction> buyLand(String name, int amount)
    {
        log.info("[{}] buying land {}", name, amount);
        Optional<KingdomEntity> maybeKingdom = kingdomRepository.getKingdomByName(name);
        if (maybeKingdom.isEmpty())
        {
            return ResponseEntity.notFound().build();
        }

        var kingdom = maybeKingdom.get();
        var action = new KingdomOtherAction(kingdom);
        var transaction = action.buyLand(amount);
        kingdomRepository.update(kingdom);

        return ResponseEntity.ok(transaction);
    }

    @Transactional
    public void addTurnForEveryone()
    {
        log.info("Adding turn for everyone");
        kingdomRepository.getAllKingdoms().stream().filter(kingdom -> !kingdomDetailsProvider.hasMaxTurns(kingdom)).forEach(kingdom ->
        {
            kingdom.addTurn();
            kingdomRepository.update(kingdom);
        });
    }

    @Transactional
    public ResponseEntity<KingdomSpecialBuildingEntity> startSpecialBuilding(String name, KingdomSpecialBuildingStartDto specialBuildingStartDto)
    {
        log.info("[{}] starting special building {}", name, specialBuildingStartDto.name());

        Optional<KingdomEntity> maybeKingdom = kingdomRepository.getKingdomByName(name);
        if (maybeKingdom.isEmpty())
        {
            return ResponseEntity.notFound().build();
        }

        var kingdom = maybeKingdom.get();
        var action = new KingdomSpecialBuildingAction(kingdom, gameConfig);
        Optional<KingdomSpecialBuildingEntity> specialBuilding = action.startSpecialBuilding(specialBuildingStartDto.name());
        kingdomRepository.update(kingdom);
        return ResponseEntity.of(specialBuilding);
    }

    @Transactional
    public ResponseEntity<Integer> buildSpecialBuilding(String name, KingdomSpecialBuildingBuildDto specialBuildingBuildDto)
    {
        log.info("[{}] building special building {}", name, specialBuildingBuildDto);
        Optional<KingdomEntity> maybeKingdom = kingdomRepository.getKingdomByName(name);
        if (maybeKingdom.isEmpty())
        {
            return ResponseEntity.notFound().build();
        }

        var kingdom = maybeKingdom.get();
        // TODO use repository please
        Optional<KingdomSpecialBuildingEntity> maybeSpecialBuilding = kingdom.getSpecialBuildings().stream().filter(specialBuilding -> specialBuilding.getId().equals(specialBuildingBuildDto.id())).findFirst();
        if (maybeSpecialBuilding.isEmpty())
        {
            return ResponseEntity.notFound().build();
        }

        var action = new KingdomSpecialBuildingAction(kingdom, gameConfig);
        var spentPoints = action.buildSpecialBuilding(maybeSpecialBuilding.get(), specialBuildingBuildDto.buildingPoints());
        kingdomRepository.update(kingdom);

        return ResponseEntity.ok(spentPoints);
    }

    @Transactional
    public ResponseEntity<Boolean> demolishSpecialBuilding(String name, KingdomSpecialBuildingDemolishDto specialBuildingDemolishDto)
    {
        log.info("[{}] demolishing special building {}", name, specialBuildingDemolishDto);
        Optional<KingdomEntity> maybeKingdom = kingdomRepository.getKingdomByName(name);
        if (maybeKingdom.isEmpty())
        {
            return ResponseEntity.notFound().build();
        }

        var kingdom = maybeKingdom.get();
        var action = new KingdomSpecialBuildingAction(kingdom, gameConfig);
        var demolished = action.demolishSpecialBuilding(specialBuildingDemolishDto.id());
        kingdomRepository.update(kingdom);

        return ResponseEntity.ok(demolished);
    }

    @Transactional
    public ResponseEntity<SendCarriersResult> sendCarriers(String kingdomName, SendCarriersDto sendCarriersDto)
    {
        log.info("[{}] sending carriers {}", kingdomName, sendCarriersDto);
        Optional<KingdomEntity> maybeKingdom = kingdomRepository.getKingdomByName(kingdomName);
        if (maybeKingdom.isEmpty())
        {
            return ResponseEntity.notFound().build();
        }
        var kingdom = maybeKingdom.get();

        // TODO no need to fetch entire kingdom, we only want to check if it exists
        Optional<KingdomEntity> maybeDestinationKingdom = kingdomRepository.getKingdomByName(sendCarriersDto.destinationKingdomName());
        if (maybeDestinationKingdom.isEmpty())
        {
            return ResponseEntity.ok(SendCarriersResult.failure("Destination kingdom does not exist"));
        }

        var action = new KingdomCarriersAction(kingdom, gameConfig);
        SendCarriersResult result = action.sendCarriers(sendCarriersDto);
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
        Optional<KingdomEntity> maybeKingdom = kingdomRepository.getKingdomByName(kingdomName);
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

        var action = new KingdomCarriersAction(kingdom, gameConfig);
        action.withdrawCarriers(carriersOnTheMove.get());
        kingdomRepository.update(kingdom);

        return ResponseEntity.ok(true);
    }

    @Transactional
    public ResponseEntity<SendAttackResult> sendAttack(String kingdomName, SendAttackDto sendAttackDto)
    {
        log.info("[{}] sending attack {}", kingdomName, sendAttackDto);
        Optional<KingdomEntity> maybeKingdom = kingdomRepository.getKingdomByName(kingdomName);
        if (maybeKingdom.isEmpty())
        {
            return ResponseEntity.notFound().build();
        }
        var kingdom = maybeKingdom.get();

        // TODO no need to fetch entire kingdom, we only want to check if it exists
        Optional<KingdomEntity> maybeDestinationKingdom = kingdomRepository.getKingdomByName(sendAttackDto.destinationKingdomName());
        if (maybeDestinationKingdom.isEmpty())
        {
            return ResponseEntity.ok(SendAttackResult.failure("Destination kingdom does not exist"));
        }

        var action = new KingdomMilitaryAction(kingdom, gameConfig);
        SendAttackResult result = action.sendAttack(sendAttackDto);
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
        Optional<KingdomEntity> maybeKingdom = kingdomRepository.getKingdomByName(kingdomName);
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

        var action = new KingdomMilitaryAction(kingdom, gameConfig);
        action.withdrawAttack(ongoingAttacks.get());
        kingdomRepository.update(kingdom);

        return ResponseEntity.ok(true);
    }
}
