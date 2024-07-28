package com.knightsofdarkness.game.kingdom;

import java.util.Optional;

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
    private final List<MarketOffer> marketOffers;
    KingdomTurnReport lastTurnReport; // TOOD consider making them all package-private
    private final KingdomBuildAction kingdomBuildAction = new KingdomBuildAction(this);
    private final KingdomTrainAction kingdomTrainAction = new KingdomTrainAction(this);
    private final KingdomMarketAction kingdomMarketAction = new KingdomMarketAction(this);
    private final KingdomOtherAction kingdomOtherAction = new KingdomOtherAction(this);

    public Kingdom(String name, GameConfig config, KingdomResources resources, KingdomBuildings buildings, KingdomUnits units, List<MarketOffer> marketOffers, KingdomTurnReport lastTurnReport)
    {
        this.name = name;
        this.config = config;
        this.resources = resources;
        this.buildings = buildings;
        this.units = units;
        this.marketOffers = marketOffers;
        this.lastTurnReport = lastTurnReport;
    }

    public KingdomBuildings build(KingdomBuildings buildingsToBuild)
    {
        return kingdomBuildAction.build(buildingsToBuild);
    }

    @Deprecated
    public int build(BuildingName building, int count)
    {
        return kingdomBuildAction.build(building, count);
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

    @Deprecated
    public List<MarketOffer> getMarketOffers()
    {
        return marketOffers;
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
}
