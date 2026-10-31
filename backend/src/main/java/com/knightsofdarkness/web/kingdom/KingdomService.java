package com.knightsofdarkness.web.kingdom;

import java.util.Optional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.random.RandomGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.knightsofdarkness.web.common.kingdom.KingdomBuildingsActionResult;
import com.knightsofdarkness.web.common.kingdom.KingdomBuildingsDto;
import com.knightsofdarkness.web.common.kingdom.KingdomDetailsDto;
import com.knightsofdarkness.web.common.kingdom.KingdomDto;
import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.kingdom.KingdomPassTurnActionResult;
import com.knightsofdarkness.web.common.kingdom.KingdomSpecialBuildingBuildDto;
import com.knightsofdarkness.web.common.kingdom.KingdomSpecialBuildingDemolishDto;
import com.knightsofdarkness.web.common.kingdom.KingdomSpecialBuildingStartDto;
import com.knightsofdarkness.web.common.kingdom.KingdomTurnReport;
import com.knightsofdarkness.web.common.kingdom.KingdomUnitsActionResult;
import com.knightsofdarkness.web.common.kingdom.LandTransaction;
import com.knightsofdarkness.web.common.kingdom.SendAttackDto;
import com.knightsofdarkness.web.common.kingdom.SendAttackResult;
import com.knightsofdarkness.web.common.kingdom.SendCarriersDto;
import com.knightsofdarkness.web.common.kingdom.SendCarriersResult;
import com.knightsofdarkness.web.common.kingdom.SendReinforcementsDto;
import com.knightsofdarkness.web.common.kingdom.SendReinforcementsResult;
import com.knightsofdarkness.web.common.kingdom.UnitsMapDto;
import com.knightsofdarkness.web.common.messaging.NotificationKind;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.kingdom.model.KingdomBuildAction;
import com.knightsofdarkness.web.kingdom.model.KingdomCarriersAction;
import com.knightsofdarkness.web.kingdom.model.KingdomCreator;
import com.knightsofdarkness.web.kingdom.model.KingdomDetailsCalculator;
import com.knightsofdarkness.web.kingdom.model.KingdomDetailsProvider;
import com.knightsofdarkness.web.kingdom.model.KingdomEntity;
import com.knightsofdarkness.web.kingdom.model.KingdomOngoingAttackEntity;
import com.knightsofdarkness.web.kingdom.model.KingdomEvent;
import com.knightsofdarkness.web.kingdom.model.KingdomEvents;
import com.knightsofdarkness.web.kingdom.model.KingdomMilitaryAction;
import com.knightsofdarkness.web.kingdom.model.KingdomOtherAction;
import com.knightsofdarkness.web.kingdom.model.KingdomReadRepository;
import com.knightsofdarkness.web.kingdom.model.KingdomRepository;
import com.knightsofdarkness.web.kingdom.model.KingdomSpecialBuildingAction;
import com.knightsofdarkness.web.kingdom.model.KingdomSpecialBuildingEntity;
import com.knightsofdarkness.web.kingdom.model.KingdomTrainAction;
import com.knightsofdarkness.web.kingdom.model.KingdomTurnAction;
import com.knightsofdarkness.web.market.model.MarketOfferReadRepository;
import com.knightsofdarkness.web.utils.Utils;
import com.knightsofdarkness.web.messaging.INotificationSystem;

@Service
public class KingdomService
{
    private static final Logger log = LoggerFactory.getLogger(KingdomService.class);
    private final KingdomRepository kingdomRepository;
    private final KingdomReadRepository kingdomReadRepository;
    private final MarketOfferReadRepository marketOfferReadRepository;
    private final GameConfig gameConfig;
    private final IKingdomInteractor kingdomInteractor;
    private final KingdomDetailsProvider kingdomDetailsProvider;
    private final INotificationSystem notificationSystem;
    private final KingdomEvents events;
    private final RandomGenerator dice = RandomGenerator.getDefault();

    public KingdomService(KingdomRepository kingdomRepository, KingdomReadRepository kingdomReadRepository, MarketOfferReadRepository marketOfferReadRepository, GameConfig gameConfig, IKingdomInteractor kingdomInteractor,
            KingdomDetailsProvider kingdomDetailsProvider, INotificationSystem notificationSystem)
    {
        this.kingdomRepository = kingdomRepository;
        this.kingdomReadRepository = kingdomReadRepository;
        this.marketOfferReadRepository = marketOfferReadRepository;
        this.gameConfig = gameConfig;
        this.kingdomInteractor = kingdomInteractor;
        this.kingdomDetailsProvider = kingdomDetailsProvider;
        this.notificationSystem = notificationSystem;
        this.events = new KingdomEvents(gameConfig);
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

    @Transactional
    public KingdomEntity createKingdom(String name)
    {
        var kingdomCreator = new KingdomCreator(gameConfig);
        var kingdom = kingdomCreator.createKingdom(name);
        return kingdomRepository.add(kingdom);
    }

    public Optional<KingdomDto> getKingdomByName(String name)
    {
        var maybeKingdom = kingdomReadRepository.getKingdomByName(name);
        if (maybeKingdom.isEmpty())
        {
            return Optional.empty();
        }
        var kingdom = maybeKingdom.get();
        var offers = marketOfferReadRepository.findByKingdomName(name);
        kingdom.marketOffers = offers;
        kingdom.incoming = kingdomRepository.getAttacksOn(name).stream().map(KingdomOngoingAttackEntity::toIncomingDto).toList();
        return Optional.of(kingdom);
    }

    @Transactional
    public Optional<KingdomBuildingsActionResult> build(String kingdomName, KingdomBuildingsDto buildings)
    {
        log.info("[{}] buildings to build {}", kingdomName, buildings);
        Optional<KingdomEntity> maybeKingdom = kingdomRepository.getKingdomByName(kingdomName);
        if (maybeKingdom.isEmpty())
        {
            return Optional.empty();
        }

        var kingdom = maybeKingdom.get();

        var buildAction = new KingdomBuildAction(kingdom, gameConfig);
        var buildingsBuilt = buildAction.build(buildings);
        kingdomRepository.update(kingdom);
        return Optional.of(buildingsBuilt);
    }

    @Transactional
    public Optional<KingdomBuildingsActionResult> demolish(String kingdomName, KingdomBuildingsDto buildings)
    {
        log.info("[{}] demolishing {}", kingdomName, buildings);
        Optional<KingdomEntity> maybeKingdom = kingdomRepository.getKingdomByName(kingdomName);
        if (maybeKingdom.isEmpty())
        {
            return Optional.empty();
        }

        var kingdom = maybeKingdom.get();

        var buildAction = new KingdomBuildAction(kingdom, gameConfig);
        var buildingsDemolished = buildAction.demolish(buildings);
        kingdomRepository.update(kingdom);
        return Optional.of(buildingsDemolished);
    }

    @Transactional
    public Optional<KingdomUnitsActionResult> train(String name, UnitsMapDto unitsToTrain)
    {
        log.info("[{}] training {}", name, unitsToTrain);
        Optional<KingdomEntity> maybeKingdom = kingdomRepository.getKingdomByName(name);
        if (maybeKingdom.isEmpty())
        {
            return Optional.empty();
        }

        var kingdom = maybeKingdom.get();
        var action = new KingdomTrainAction(kingdom, gameConfig);
        var unitsTrained = action.train(unitsToTrain);
        kingdomRepository.update(kingdom);

        return Optional.of(unitsTrained);
    }

    @Transactional
    public Optional<KingdomUnitsActionResult> fireUnits(String name, UnitsMapDto unitsToFire)
    {
        log.info("[{}] firing {}", name, unitsToFire);
        Optional<KingdomEntity> maybeKingdom = kingdomRepository.getKingdomByName(name);
        if (maybeKingdom.isEmpty())
        {
            return Optional.empty();
        }

        var kingdom = maybeKingdom.get();
        var action = new KingdomTrainAction(kingdom, gameConfig);
        var unitsFired = action.fireUnits(unitsToFire);
        kingdomRepository.update(kingdom);

        return Optional.of(unitsFired);
    }

    @Transactional
    public Optional<KingdomPassTurnActionResult> passTurn(String name, int weaponsProductionPercentage)
    {
        log.info("[{}] passing turn", name);
        Optional<KingdomEntity> maybeKingdom = kingdomRepository.getKingdomByName(name);
        if (maybeKingdom.isEmpty())
        {
            return Optional.empty();
        }

        var kingdom = maybeKingdom.get();
        var action = new KingdomTurnAction(kingdom, kingdomInteractor, gameConfig, kingdomDetailsProvider);
        var result = action.passTurn(weaponsProductionPercentage);

        if (result.success())
        {
            var kinds = KingdomEvent.values();
            result.turnReport().flatMap(report -> events.happen(kingdom, report, dice.nextDouble(), kinds[dice.nextInt(kinds.length)]))
                    .ifPresent(event -> notificationSystem.create(name, NotificationKind.event, event));
            kingdomRepository.update(kingdom);
        }

        return Optional.of(result);
    }

    @Transactional(readOnly = true)
    public List<KingdomDto> getAllKingdoms()
    {
        return kingdomRepository.getAllKingdoms().stream().map(KingdomEntity::toDto).toList();
    }

    @Transactional(readOnly = true)
    public Optional<KingdomDetailsDto> getKingdomDetails(String name, int weaponsProductionPercentage)
    {
        return kingdomRepository.getKingdomByName(name).map(kingdom -> new KingdomDetailsCalculator(gameConfig).calculate(kingdom, weaponsProductionPercentage));
    }

    @Transactional(readOnly = true)
    public Optional<LandTransaction> quoteLand(String name, int amount)
    {
        return kingdomRepository.getKingdomByName(name).map(kingdom -> KingdomOtherAction.calculateCost(kingdom.getResources().getCount(ResourceName.land), amount, kingdom.getResources().getCount(ResourceName.gold)));
    }

    @Transactional
    public Optional<LandTransaction> buyLand(String name, int amount)
    {
        log.info("[{}] buying land {}", name, amount);
        Optional<KingdomEntity> maybeKingdom = kingdomRepository.getKingdomByName(name);
        if (maybeKingdom.isEmpty())
        {
            return Optional.empty();
        }

        var kingdom = maybeKingdom.get();
        var action = new KingdomOtherAction(kingdom);
        var transaction = action.buyLand(amount);
        kingdomRepository.update(kingdom);

        return Optional.of(transaction);
    }

    @Transactional
    public void addTurnForEveryone()
    {
        var kingdoms = kingdomRepository.addTurnBelow(gameConfig.common().maxTurns());
        kingdomRepository.countDownProtection();
        log.info("Added a turn to {} kingdoms", kingdoms);
    }

    @Transactional
    public Optional<KingdomSpecialBuildingEntity> startSpecialBuilding(String name, KingdomSpecialBuildingStartDto specialBuildingStartDto)
    {
        log.info("[{}] starting special building {}", name, specialBuildingStartDto.name());

        Optional<KingdomEntity> maybeKingdom = kingdomRepository.getKingdomByName(name);
        if (maybeKingdom.isEmpty())
        {
            return Optional.empty();
        }

        var kingdom = maybeKingdom.get();
        var action = new KingdomSpecialBuildingAction(kingdom, gameConfig);
        Optional<KingdomSpecialBuildingEntity> specialBuilding = action.startSpecialBuilding(specialBuildingStartDto.name());
        kingdomRepository.update(kingdom);
        return specialBuilding;
    }

    @Transactional
    public Optional<Integer> buildSpecialBuilding(String name, KingdomSpecialBuildingBuildDto specialBuildingBuildDto)
    {
        log.info("[{}] building special building {}", name, specialBuildingBuildDto);
        Optional<KingdomEntity> maybeKingdom = kingdomRepository.getKingdomByName(name);
        if (maybeKingdom.isEmpty())
        {
            return Optional.empty();
        }

        var kingdom = maybeKingdom.get();
        Optional<KingdomSpecialBuildingEntity> maybeSpecialBuilding = kingdom.getSpecialBuildings().stream().filter(specialBuilding -> specialBuilding.getId().equals(specialBuildingBuildDto.id())).findFirst();
        if (maybeSpecialBuilding.isEmpty())
        {
            return Optional.empty();
        }

        var action = new KingdomSpecialBuildingAction(kingdom, gameConfig);
        var spentPoints = action.buildSpecialBuilding(maybeSpecialBuilding.get(), specialBuildingBuildDto.buildingPoints());
        kingdomRepository.update(kingdom);

        return Optional.of(spentPoints);
    }

    @Transactional
    public Optional<Boolean> demolishSpecialBuilding(String name, KingdomSpecialBuildingDemolishDto specialBuildingDemolishDto)
    {
        log.info("[{}] demolishing special building {}", name, specialBuildingDemolishDto);
        Optional<KingdomEntity> maybeKingdom = kingdomRepository.getKingdomByName(name);
        if (maybeKingdom.isEmpty())
        {
            return Optional.empty();
        }

        var kingdom = maybeKingdom.get();
        var action = new KingdomSpecialBuildingAction(kingdom, gameConfig);
        var demolished = action.demolishSpecialBuilding(specialBuildingDemolishDto.id());
        kingdomRepository.update(kingdom);

        return Optional.of(demolished);
    }

    @Transactional
    public Optional<SendCarriersResult> sendCarriers(String kingdomName, SendCarriersDto sendCarriersDto)
    {
        log.info("[{}] sending carriers {}", kingdomName, sendCarriersDto);
        Optional<KingdomEntity> maybeKingdom = kingdomRepository.getKingdomByName(kingdomName);
        if (maybeKingdom.isEmpty())
        {
            return Optional.empty();
        }
        var kingdom = maybeKingdom.get();

        Optional<KingdomEntity> maybeDestinationKingdom = kingdomRepository.getKingdomByName(sendCarriersDto.destinationKingdomName());
        if (maybeDestinationKingdom.isEmpty())
        {
            return Optional.of(SendCarriersResult.failure("Destination kingdom does not exist"));
        }

        var action = new KingdomCarriersAction(kingdom, gameConfig);
        SendCarriersResult result = action.sendCarriers(sendCarriersDto);
        if (result.success())
        {
            kingdomRepository.update(kingdom);
        }

        return Optional.of(result);
    }

    @Transactional
    public Optional<Boolean> withdrawCarriers(String kingdomName, UUID carriersOnTheMoveId)
    {
        log.info("[{}] withdrawing carriers {}", kingdomName, carriersOnTheMoveId);
        Optional<KingdomEntity> maybeKingdom = kingdomRepository.getKingdomByName(kingdomName);
        if (maybeKingdom.isEmpty())
        {
            return Optional.empty();
        }
        var kingdom = maybeKingdom.get();
        var carriersOnTheMove = kingdom.getCarriersOnTheMove().stream().filter(c -> c.getId().equals(carriersOnTheMoveId)).findFirst();
        if (carriersOnTheMove.isEmpty())
        {
            return Optional.empty();
        }

        var action = new KingdomCarriersAction(kingdom, gameConfig);
        action.withdrawCarriers(carriersOnTheMove.get());
        kingdomRepository.update(kingdom);

        return Optional.of(true);
    }

    @Transactional
    public Optional<SendAttackResult> sendAttack(String kingdomName, SendAttackDto sendAttackDto)
    {
        log.info("[{}] sending attack {}", kingdomName, sendAttackDto);
        Optional<KingdomEntity> maybeKingdom = kingdomRepository.getKingdomByName(kingdomName);
        if (maybeKingdom.isEmpty())
        {
            return Optional.empty();
        }
        var kingdom = maybeKingdom.get();

        Optional<KingdomEntity> maybeDestinationKingdom = kingdomRepository.getKingdomByName(sendAttackDto.destinationKingdomName());
        if (maybeDestinationKingdom.isEmpty())
        {
            return Optional.of(SendAttackResult.failure("Destination kingdom does not exist"));
        }

        var action = new KingdomMilitaryAction(kingdom, gameConfig);
        SendAttackResult result = action.sendAttack(sendAttackDto, maybeDestinationKingdom.get());
        if (result.success())
        {
            kingdomRepository.update(kingdom);
            var army = kingdom.getOngoingAttacks().get(kingdom.getOngoingAttacks().size() - 1).toIncomingDto();
            notificationSystem.create(army.to(), NotificationKind.threat,
                    Utils.format("Our scouts saw {} with about {} soldiers march on us, they arrive in {} of their turns", army.from(), army.soldiers(), army.turnsLeft()));
        }

        return Optional.of(result);
    }

    @Transactional
    public Optional<SendReinforcementsResult> sendReinforcements(String kingdomName, SendReinforcementsDto order)
    {
        log.info("[{}] sending reinforcements to {}", kingdomName, order.hostKingdomName());
        var maybeKingdom = kingdomRepository.getKingdomByName(kingdomName);
        if (maybeKingdom.isEmpty())
        {
            return Optional.empty();
        }
        var maybeHost = kingdomRepository.getKingdomByName(order.hostKingdomName());
        if (maybeHost.isEmpty())
        {
            return Optional.of(SendReinforcementsResult.failure("No such kingdom"));
        }

        var kingdom = maybeKingdom.get();
        var result = new KingdomMilitaryAction(kingdom, gameConfig).sendReinforcements(order, maybeHost.get());
        if (result.success())
        {
            kingdomRepository.update(kingdom);
        }
        return Optional.of(result);
    }

    @Transactional
    public Optional<Boolean> recallReinforcements(String kingdomName, UUID reinforcementId)
    {
        log.info("[{}] recalling reinforcements {}", kingdomName, reinforcementId);
        var maybeKingdom = kingdomRepository.getKingdomByName(kingdomName);
        if (maybeKingdom.isEmpty())
        {
            return Optional.empty();
        }
        var kingdom = maybeKingdom.get();
        var reinforcement = kingdom.getReinforcements().stream().filter(candidate -> candidate.getId().equals(reinforcementId)).findFirst();
        if (reinforcement.isEmpty())
        {
            return Optional.empty();
        }

        new KingdomMilitaryAction(kingdom, gameConfig).recallReinforcements(reinforcement.get());
        kingdomRepository.update(kingdom);
        return Optional.of(true);
    }

    @Transactional
    public Optional<Boolean> withdrawAttack(String kingdomName, UUID attackId)
    {
        log.info("[{}] withdrawing attack {}", kingdomName, attackId);
        Optional<KingdomEntity> maybeKingdom = kingdomRepository.getKingdomByName(kingdomName);
        if (maybeKingdom.isEmpty())
        {
            return Optional.empty();
        }
        var kingdom = maybeKingdom.get();
        var ongoingAttacks = kingdom.getOngoingAttacks().stream().filter(c -> c.getId().equals(attackId)).findFirst();
        if (ongoingAttacks.isEmpty())
        {
            return Optional.empty();
        }

        var action = new KingdomMilitaryAction(kingdom, gameConfig);
        action.withdrawAttack(ongoingAttacks.get());
        kingdomRepository.update(kingdom);

        return Optional.of(true);
    }

}
