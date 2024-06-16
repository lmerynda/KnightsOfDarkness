package com.knightsofdarkness.game.kingdom;

import java.util.List;

import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.game.market.IMarket;
import com.knightsofdarkness.game.market.MarketOffer;
import com.knightsofdarkness.game.market.MarketResource;

public class Kingdom {
    private final String name;
    private final IMarket market;
    private final GameConfig config;
    private final KingdomResources resources;
    private final KingdomBuildings buildings;
    private final KingdomUnits units;
    private final List<MarketOffer> marketOffers;
    private final KingdomBuildAction kingdomBuildAction = new KingdomBuildAction(this);
    private final KingdomTrainAction kingdomTrainAction = new KingdomTrainAction(this);
    private final KingdomTurnAction kingdomTurnAction = new KingdomTurnAction(this);
    private final KingdomMarketAction kingdomMarketAction = new KingdomMarketAction(this);
    private final KingdomOtherAction kingdomOtherAction = new KingdomOtherAction(this);

    public Kingdom(String name, GameConfig config, IMarket market, KingdomResources resources, KingdomBuildings buildings, KingdomUnits units, List<MarketOffer> marketOffers)
    {
        this.name = name;
        this.config = config;
        this.market = market;
        this.resources = resources;
        this.buildings = buildings;
        this.units = units;
        this.marketOffers = marketOffers;
    }

    public int build(KingdomBuildings buildingsToBuild)
    {
        return kingdomBuildAction.build(buildingsToBuild);
    }

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

    public boolean passTurn()
    {
        return kingdomTurnAction.passTurn();
    }

    public int getTotalPeopleCount()
    {
        return units.countAll() + resources.getCount(ResourceName.unemployed);
    }

    public int getUnusedLand()
    {
        return resources.getCount(ResourceName.land) - buildings.countAll();
    }

    public int buyLand(int count)
    {
        return kingdomOtherAction.buyLand(count);
    }

    public List<MarketOffer> getMarketOffers()
    {
        return marketOffers;
    }

    public int buyMarketOffer(MarketOffer offer, int amount)
    {
        return this.kingdomMarketAction.buyMarketOffer(offer, amount);
    }

    public void postMarketOffer(MarketResource resource, int count, int price)
    {
        this.kingdomMarketAction.postOffer(resource, count, price);
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

    public IMarket getMarket()
    {
        return market;
    }

    public String getName()
    {
        return name;
    }
}
