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
import com.knightsofdarkness.common.kingdom.KingdomUnitsDto;
import com.knightsofdarkness.common.kingdom.LandTransaction;
import com.knightsofdarkness.common.kingdom.ResourceName;
import com.knightsofdarkness.common.kingdom.SendCarriersDto;
import com.knightsofdarkness.common.kingdom.SendCarriersResult;
import com.knightsofdarkness.common.kingdom.SpecialBuildingType;
import com.knightsofdarkness.common.kingdom.UnitName;
import com.knightsofdarkness.common.market.MarketResource;
import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.game.market.MarketOffer;

public class Kingdom {
    private final String name;
    private final GameConfig config;
    private final KingdomResources resources;
    private final KingdomBuildings buildings;
    private final KingdomUnits units;
    private final List<KingdomSpecialBuilding> specialBuildings;
    KingdomTurnReport lastTurnReport; // TOOD consider making them all package-private
    private final KingdomBuildAction kingdomBuildAction = new KingdomBuildAction(this);
    private final KingdomTrainAction kingdomTrainAction = new KingdomTrainAction(this);
    private final KingdomMarketAction kingdomMarketAction = new KingdomMarketAction(this);
    private final KingdomOtherAction kingdomOtherAction = new KingdomOtherAction(this);
    private final KingdomSpecialBuildingAction kingdomSpecialBuildingAction = new KingdomSpecialBuildingAction(this);

    public Kingdom(String name, GameConfig config, KingdomResources resources, KingdomBuildings buildings, List<KingdomSpecialBuilding> specialBuildings, KingdomUnits units, KingdomTurnReport lastTurnReport)
    {
        this.name = name;
        this.config = config;
        this.resources = resources;
        this.buildings = buildings;
        this.units = units;
        this.lastTurnReport = lastTurnReport;
        this.specialBuildings = specialBuildings;
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

    public KingdomUnitsActionResult train(KingdomUnitsDto unitsToTrain)
    {
        return kingdomTrainAction.train(unitsToTrain);
    }

    public KingdomPassTurnActionResult passTurn()
    {
        return new KingdomTurnAction(this).passTurn();
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
        var resource = sendCarriersDto.resource();
        int singleCarrierCapacity = config.carrierCapacity().get(resource);
        int carriersCapacity = units.getCount(UnitName.carrier) * singleCarrierCapacity;
        if (carriersCapacity <= 0)
        {
            return SendCarriersResult.failure("You have not enough carriers to send");
        }

        int amountPossibleToSend = Math.min(resources.getCount(ResourceName.from(resource)), sendCarriersDto.amount());
        int amountToSend = Math.min(amountPossibleToSend, carriersCapacity);

        if (amountToSend <= 0)
        {
            return SendCarriersResult.failure("You have not enough resources to send");
        }

        resources.subtractCount(ResourceName.from(resource), amountToSend);
        units.subtractCount(UnitName.carrier, (int) Math.ceil((double) amountToSend / singleCarrierCapacity));

        // TODO handle turn ticks and return carriers to the kingdom
        return SendCarriersResult.success("Carriers sent and should arrive in a few turns", new SendCarriersDto(sendCarriersDto.destinationKingdomName(), resource, amountToSend));
    }
}
