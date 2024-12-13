package com.knightsofdarkness.game.kingdom;

import java.util.Optional;

import java.util.List;
import java.util.UUID;

import com.knightsofdarkness.common.kingdom.BuildingName;
import com.knightsofdarkness.common.kingdom.KingdomBuildingsActionResult;
import com.knightsofdarkness.common.kingdom.KingdomBuildingsDto;
import com.knightsofdarkness.common.kingdom.KingdomPassTurnActionResult;
import com.knightsofdarkness.common.kingdom.KingdomTurnReport;
import com.knightsofdarkness.common.kingdom.KingdomUnitsActionResult;
import com.knightsofdarkness.common.kingdom.LandTransaction;
import com.knightsofdarkness.common.kingdom.ResourceName;
import com.knightsofdarkness.common.kingdom.SendAttackDto;
import com.knightsofdarkness.common.kingdom.SendAttackResult;
import com.knightsofdarkness.common.kingdom.SendCarriersDto;
import com.knightsofdarkness.common.kingdom.SendCarriersResult;
import com.knightsofdarkness.common.kingdom.SpecialBuildingType;
import com.knightsofdarkness.common.kingdom.UnitName;
import com.knightsofdarkness.common.kingdom.UnitsMapDto;
import com.knightsofdarkness.common.market.MarketResource;
import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.game.interactions.IKingdomInteractor;
import com.knightsofdarkness.game.market.MarketOffer;

public class Kingdom {
    private final String name;
    private final GameConfig config;
    private final KingdomResources resources;
    private final KingdomBuildings buildings;
    private final KingdomUnits units;
    private final List<KingdomSpecialBuilding> specialBuildings;
    private final List<KingdomCarriersOnTheMove> carriersOnTheMove;
    private final List<KingdomOngoingAttack> ongoingAttacks;
    KingdomTurnReport lastTurnReport; // TOOD consider making them all package-private
    private final KingdomBuildAction kingdomBuildAction = new KingdomBuildAction(this);
    private final KingdomTrainAction kingdomTrainAction = new KingdomTrainAction(this);
    private final KingdomMarketAction kingdomMarketAction = new KingdomMarketAction(this);
    private final KingdomOtherAction kingdomOtherAction = new KingdomOtherAction(this);
    private final KingdomSpecialBuildingAction kingdomSpecialBuildingAction = new KingdomSpecialBuildingAction(this);
    private final KingdomCarriersAction kingdomCarriersAction = new KingdomCarriersAction(this);
    private final KingdomMilitaryAction kingdomMilitaryAction = new KingdomMilitaryAction(this);

    public Kingdom(String name, GameConfig config, KingdomResources resources, KingdomBuildings buildings, List<KingdomSpecialBuilding> specialBuildings, List<KingdomCarriersOnTheMove> carriersOnTheMove,
            List<KingdomOngoingAttack> ongoingAttacks, KingdomUnits units, KingdomTurnReport lastTurnReport)
    {
        this.name = name;
        this.config = config;
        this.resources = resources;
        this.buildings = buildings;
        this.units = units;
        this.lastTurnReport = lastTurnReport;
        this.specialBuildings = specialBuildings;
        this.carriersOnTheMove = carriersOnTheMove;
        this.ongoingAttacks = ongoingAttacks;
    }

    public KingdomBuildingsActionResult build(KingdomBuildingsDto buildingsToBuild)
    {
        return kingdomBuildAction.build(buildingsToBuild);
    }

    public KingdomBuildingsActionResult demolish(KingdomBuildingsDto buildingsToDemolish)
    {
        return kingdomBuildAction.demolish(buildingsToDemolish);
    }

    public int getBuildingCapacity(BuildingName name)
    {
        return buildings.getCapacity(name, config.buildingCapacity().getCapacity(name));
    }

    public KingdomUnitsActionResult train(UnitsMapDto unitsToTrain)
    {
        return kingdomTrainAction.train(unitsToTrain);
    }

    public KingdomPassTurnActionResult passTurn(IKingdomInteractor kingdomInteractor)
    {
        return new KingdomTurnAction(this, kingdomInteractor).passTurn();
    }

    public void addTurn()
    {
        if (!hasMaxTurns())
        {
            resources.addCount(ResourceName.turns, 1);
        }
    }

    public int getTotalPeopleCount()
    {
        return units.countAll() + resources.getCount(ResourceName.unemployed);
    }

    public int getUnusedLand()
    {
        return resources.getCount(ResourceName.land) - buildings.countAll();
    }

    public int getOccupiedLand()
    {
        return buildings.countAll();
    }

    public LandTransaction buyLand(int count)
    {
        return kingdomOtherAction.buyLand(count);
    }

    public int postMarketOffer(MarketResource resource, int count)
    {
        return this.kingdomMarketAction.postOffer(resource, count);
    }

    public void withdrawMarketOffer(MarketOffer offer)
    {
        this.kingdomMarketAction.withdrawMarketOffer(offer);
    }

    public void acceptMarketOffer(int goldValue)
    {
        this.kingdomMarketAction.acceptOffer(goldValue);
    }

    public int getFoodUpkeep()
    {
        return units.countAll() * config.common().foodUpkeepPerUnit();
    }

    public KingdomResources getResources()
    {
        return resources;
    }

    public KingdomBuildings getBuildings()
    {
        return buildings;
    }

    public KingdomUnits getUnits()
    {
        return units;
    }

    public GameConfig getConfig()
    {
        return config;
    }

    public String getName()
    {
        return name;
    }

    public KingdomTurnReport getLastTurnReport()
    {
        return lastTurnReport;
    }

    public int reserveGoldForOffer(int price, int amount)
    {
        return kingdomMarketAction.reserveGoldForOffer(price, amount);
    }

    public void deliverResourcesFromOffer(MarketResource resource, int amount)
    {
        kingdomMarketAction.deliverResourcesFromOffer(resource, amount);
    }

    public int getIronUpkeep(double nourishmentProductionFactor)
    {
        // unfed blacksmith who don't work, will not consume any iron either
        var production = units.getCount(UnitName.blacksmith) * nourishmentProductionFactor * config.production().getProductionRate(UnitName.blacksmith) * config.common().ironConsumptionPerProductionUnit();
        return ((int) Math.ceil(production));
    }

    public boolean hasMaxTurns()
    {
        return resources.getCount(ResourceName.turns) >= config.common().maxTurns();
    }

    public Optional<KingdomSpecialBuilding> startSpecialBuilding(SpecialBuildingType name)
    {
        return kingdomSpecialBuildingAction.startSpecialBuilding(name);
    }

    public List<KingdomSpecialBuilding> getSpecialBuildings()
    {
        return specialBuildings;
    }

    public Optional<KingdomSpecialBuilding> getLowestLevelSpecialBuilding()
    {
        return kingdomSpecialBuildingAction.getLowestLevelSpecialBuilding();
    }

    public int buildSpecialBuilding(KingdomSpecialBuilding specialBuilding, int buildingPoints)
    {
        return kingdomSpecialBuildingAction.buildSpecialBuilding(specialBuilding, buildingPoints);
    }

    public boolean demolishSpecialBuilding(UUID id)
    {
        return kingdomSpecialBuildingAction.demolishSpecialBuilding(id);
    }

    public SendCarriersResult sendCarriers(SendCarriersDto sendCarriersDto)
    {
        return kingdomCarriersAction.sendCarriers(sendCarriersDto);
    }

    public List<KingdomCarriersOnTheMove> getCarriersOnTheMove()
    {
        return carriersOnTheMove;
    }

    public List<KingdomOngoingAttack> getOngoingAttacks()
    {
        return ongoingAttacks;
    }

    public void receiveResourceTransfer(MarketResource resource, int amount)
    {
        resources.addCount(ResourceName.from(resource), amount);
    }

    public void withdrawCarriers(KingdomCarriersOnTheMove carriersOnTheMove)
    {
        kingdomCarriersAction.withdrawCarriers(carriersOnTheMove);
    }

    public SendAttackResult sendAttack(SendAttackDto sendAttackDto)
    {
        return kingdomMilitaryAction.sendAttack(sendAttackDto);
    }

    public void withdrawAttack(KingdomOngoingAttack ongoingAttack)
    {
        kingdomMilitaryAction.withdrawAttack(ongoingAttack);
    }
}
