package com.knightsofdarkness.game.kingdom;

import java.util.Optional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.game.market.MarketOffer;
import com.knightsofdarkness.game.market.MarketResource;

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

    public KingdomBuildings build(KingdomBuildings buildingsToBuild)
    {
        return kingdomBuildAction.build(buildingsToBuild);
    }

    public int getBuildingCapacity(BuildingName name)
    {
        return buildings.getCapacity(name, config.buildingCapacity().getCapacity(name));
    }

    public KingdomUnits train(KingdomUnits unitsToTrain)
    {
        return kingdomTrainAction.train(unitsToTrain);
    }

    public Optional<KingdomTurnReport> passTurn()
    {
        return new KingdomTurnAction(this).passTurn();
    }

    public void addTurn()
    {
        // TODO make it game constant
        if (resources.getCount(ResourceName.turns) < 36)
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
        // TODO move magic value to config
        return units.countAll() * 10;
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
        // TODO have the rate somewhere in the config
        int ironConsumptionPerOneProductionUnit = 1;
        // unfed blacksmith who don't work, will not consume any iron either
        var production = units.getCount(UnitName.blacksmith) * nourishmentProductionFactor * config.production().getProductionRate(UnitName.blacksmith) * ironConsumptionPerOneProductionUnit;
        return ((int) Math.ceil(production));
    }

    public boolean hasMaxTurns()
    {
        // TODO make max turns a game constant
        return resources.getCount(ResourceName.turns) >= 36;
    }

    public Optional<KingdomSpecialBuilding> startSpecialBuilding(SpecialBuildingType name)
    {
        // TODO move to game consts
        if (specialBuildings.size() >= 5)
        {
            return Optional.empty();
        }

        var specialBuilding = new KingdomSpecialBuilding(name);
        specialBuildings.add(specialBuilding);

        return Optional.of(specialBuilding);
    }

    public List<KingdomSpecialBuilding> getSpecialBuildings()
    {
        return specialBuildings;
    }

    public Optional<KingdomSpecialBuilding> getLowestLevelSpecialBuilding()
    {
        if (specialBuildings.isEmpty())
        {
            return Optional.empty();
        }

        return Optional.of(Collections.min(specialBuildings, Comparator.comparingInt(KingdomSpecialBuilding::getLevel)));
    }

    public int buildSpecialBuilding(KingdomSpecialBuilding specialBuilding, int buildingPoints)
    {
        if (specialBuilding.isMaxLevel())
        {
            // log.info("[{}] special building {} is at max level", name, specialBuilding.getBuildingType());
            return 0;
        }
        // TODO encapsulate this functionality
        var buildingPointsRemaining = specialBuilding.getBuildingPointsRequired() - specialBuilding.getBuildingPointsPut();
        var kingdomBuildingPoints = resources.getCount(ResourceName.buildingPoints);
        if (buildingPoints >= buildingPointsRemaining)
        {
            var buildingPointsToSpend = Math.min(kingdomBuildingPoints, buildingPointsRemaining);
            resources.subtractCount(ResourceName.buildingPoints, buildingPointsToSpend);
            specialBuilding.buildingPointsPut = 0;
            specialBuilding.level++;
            specialBuilding.buildingPointsRequired *= 2;
            if (specialBuilding.level >= 5)
            {
                specialBuilding.isMaxLevel = true;
                specialBuilding.buildingPointsRequired = 0;
            }
            return buildingPointsToSpend;
        } else
        {
            var buildingPointsToSpend = Math.min(kingdomBuildingPoints, buildingPoints);
            resources.subtractCount(ResourceName.buildingPoints, buildingPointsToSpend);
            specialBuilding.buildingPointsPut += buildingPointsToSpend;
            return buildingPointsToSpend;
        }
    }
}
