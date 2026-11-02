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

import com.knightsofdarkness.web.common.kingdom.KingdomBuildingsDto;
import com.knightsofdarkness.web.common.kingdom.KingdomDetailsDto;
import com.knightsofdarkness.web.common.kingdom.KingdomDto;
import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.ActionResult;
import com.knightsofdarkness.web.common.kingdom.KingdomSpecialBuildingBuildDto;
import com.knightsofdarkness.web.common.kingdom.KingdomSpecialBuildingDemolishDto;
import com.knightsofdarkness.web.common.kingdom.KingdomSpecialBuildingStartDto;
import com.knightsofdarkness.web.common.kingdom.KingdomTurnReport;
import com.knightsofdarkness.web.common.kingdom.LandTransaction;
import com.knightsofdarkness.web.common.kingdom.SendAttackDto;
import com.knightsofdarkness.web.common.kingdom.SendCarriersDto;
import com.knightsofdarkness.web.common.kingdom.SendReinforcementsDto;
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
    public Optional<ActionResult<KingdomBuildingsDto>> build(String kingdomName, KingdomBuildingsDto buildings)
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
    public Optional<ActionResult<KingdomBuildingsDto>> demolish(String kingdomName, KingdomBuildingsDto buildings)
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
    public Optional<ActionResult<UnitsMapDto>> train(String name, UnitsMapDto unitsToTrain)
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
    public Optional<ActionResult<UnitsMapDto>> fireUnits(String name, UnitsMapDto unitsToFire)
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
    public Optional<ActionResult<KingdomTurnReport>> passTurn(String name, int weaponsProductionPercentage)
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
            result.data().flatMap(report -> events.happen(kingdom, report, dice.nextDouble(), kinds[dice.nextInt(kinds.length)]))
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
    public Optional<LandTransaction> quoteLand(String name, int count)
    {
        return kingdomRepository.getKingdomByName(name).map(kingdom -> KingdomOtherAction.calculateCost(kingdom.getResources().getCount(ResourceName.land), count, kingdom.getResources().getCount(ResourceName.gold)));
    }

    @Transactional
    public Optional<ActionResult<LandTransaction>> buyLand(String name, int count)
    {
        log.info("[{}] buying land {}", name, count);
        Optional<KingdomEntity> maybeKingdom = kingdomRepository.getKingdomByName(name);
        if (maybeKingdom.isEmpty())
        {
            return Optional.empty();
        }

        var kingdom = maybeKingdom.get();
        var action = new KingdomOtherAction(kingdom);
        var transaction = action.buyLand(count);
        kingdomRepository.update(kingdom);

        return Optional.of(transaction.count() > 0 ? ActionResult.success(Utils.format("Bought {} acres for {} gold", transaction.count(), transaction.cost()), transaction)
                : ActionResult.failure("Not enough gold for any land"));
    }

    @Transactional
    public void addTurnForEveryone()
    {
        var kingdoms = kingdomRepository.addTurnBelow(gameConfig.common().maxTurns());
        kingdomRepository.countDownProtection();
        log.info("Added a turn to {} kingdoms", kingdoms);
    }

    @Transactional
    public Optional<ActionResult<UUID>> startSpecialBuilding(String name, KingdomSpecialBuildingStartDto specialBuildingStartDto)
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
        return Optional.of(specialBuilding.map(started -> ActionResult.success(Utils.format("Ground broken for a {}", specialBuildingStartDto.name()), started.getId()))
                .orElseGet(() -> ActionResult.failure("No place for another great work")));
    }

    @Transactional
    public Optional<ActionResult<Integer>> buildSpecialBuilding(String name, KingdomSpecialBuildingBuildDto specialBuildingBuildDto)
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
            return Optional.of(ActionResult.failure("No such great work"));
        }

        var action = new KingdomSpecialBuildingAction(kingdom, gameConfig);
        var spentPoints = action.buildSpecialBuilding(maybeSpecialBuilding.get(), specialBuildingBuildDto.buildingPoints());
        kingdomRepository.update(kingdom);

        return Optional.of(spentPoints > 0 ? ActionResult.success(Utils.format("Invested {} building points", spentPoints), spentPoints)
                : ActionResult.failure("No building points went in"));
    }

    @Transactional
    public Optional<ActionResult<Void>> demolishSpecialBuilding(String name, KingdomSpecialBuildingDemolishDto specialBuildingDemolishDto)
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

        return Optional.of(demolished ? ActionResult.success("The great work was torn down") : ActionResult.failure("No such great work"));
    }

    @Transactional
    public Optional<ActionResult<SendCarriersDto>> sendCarriers(String kingdomName, SendCarriersDto sendCarriersDto)
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
            return Optional.of(ActionResult.failure("Destination kingdom does not exist"));
        }

        var action = new KingdomCarriersAction(kingdom, gameConfig);
        ActionResult<SendCarriersDto> result = action.sendCarriers(sendCarriersDto);
        if (result.success())
        {
            kingdomRepository.update(kingdom);
        }

        return Optional.of(result);
    }

    @Transactional
    public Optional<ActionResult<Void>> withdrawCarriers(String kingdomName, UUID carriersOnTheMoveId)
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
            return Optional.of(ActionResult.failure("Those carriers are no longer on the road"));
        }

        var action = new KingdomCarriersAction(kingdom, gameConfig);
        action.withdrawCarriers(carriersOnTheMove.get());
        kingdomRepository.update(kingdom);

        return Optional.of(ActionResult.success("The carriers turned back home"));
    }

    @Transactional
    public Optional<ActionResult<SendAttackDto>> sendAttack(String kingdomName, SendAttackDto sendAttackDto)
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
            return Optional.of(ActionResult.failure("Destination kingdom does not exist"));
        }

        KingdomOngoingAttackEntity lead = null;
        if (sendAttackDto.joins() != null)
        {
            var maybeLead = strikeLead(sendAttackDto.destinationKingdomName(), sendAttackDto.joins());
            if (maybeLead.isEmpty())
            {
                return Optional.of(ActionResult.failure("That army has already landed"));
            }
            lead = maybeLead.get();
        }

        var action = new KingdomMilitaryAction(kingdom, gameConfig);
        ActionResult<SendAttackDto> result = action.sendAttack(sendAttackDto, maybeDestinationKingdom.get(), lead);
        if (result.success())
        {
            kingdomRepository.update(kingdom);
            var army = kingdom.getOngoingAttacks().get(kingdom.getOngoingAttacks().size() - 1).toIncomingDto();
            notificationSystem.create(army.to(), NotificationKind.threat,
                    Utils.format("Our scouts saw {} with about {} soldiers march on us, they arrive in {} of their turns", army.from(), army.soldiers(), army.turnsLeft()));
            if (lead != null)
            {
                notificationSystem.create(lead.getKingdom().getName(), NotificationKind.alliance,
                        Utils.format("{} joins our strike on {} with {} soldiers, they wait at the border and fight right after our army lands", kingdomName, army.to(),
                                army.soldiers()));
            }
        }

        return Optional.of(result);
    }

    /**
     * the army that sets off a strike: the one asked for, or the army it joins itself while that one is still on the march
     */
    private Optional<KingdomOngoingAttackEntity> strikeLead(String target, UUID attackId)
    {
        var marching = kingdomRepository.getAttacksOn(target);
        var asked = marching.stream().filter(attack -> attack.getId().equals(attackId)).findFirst();
        return asked.map(attack -> marching.stream().filter(other -> other.getId().equals(attack.getJoinsAttackId())).findFirst().orElse(attack));
    }

    @Transactional
    public Optional<ActionResult<Void>> sendReinforcements(String kingdomName, SendReinforcementsDto order)
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
            return Optional.of(ActionResult.failure("No such kingdom"));
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
    public Optional<ActionResult<Void>> recallReinforcements(String kingdomName, UUID reinforcementId)
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
            return Optional.of(ActionResult.failure("Those soldiers are no longer away"));
        }

        new KingdomMilitaryAction(kingdom, gameConfig).recallReinforcements(reinforcement.get());
        kingdomRepository.update(kingdom);
        return Optional.of(ActionResult.success("Our soldiers are home again"));
    }

    @Transactional
    public Optional<ActionResult<Void>> withdrawAttack(String kingdomName, UUID attackId)
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
            return Optional.of(ActionResult.failure("That army is no longer on the march"));
        }

        var action = new KingdomMilitaryAction(kingdom, gameConfig);
        action.withdrawAttack(ongoingAttacks.get());
        kingdomRepository.update(kingdom);

        return Optional.of(ActionResult.success("The army turned back home"));
    }

}
