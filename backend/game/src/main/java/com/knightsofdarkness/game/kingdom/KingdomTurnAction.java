package com.knightsofdarkness.game.kingdom;

public class KingdomTurnAction {
    private final Kingdom kingdom;

    public KingdomTurnAction(Kingdom kingdom)
    {
        this.kingdom = kingdom;
    }

    public boolean passTurn()
    {
        if (kingdom.getResources().getCount(ResourceName.turns) <= 0)
        {
            return false;
        }

        kingdom.getResources().subtractCount(ResourceName.turns, 1);
        double fedPeopleRatio = eatFood();
        doProduction(fedPeopleRatio);
        // TODO food production should happen before consumption
        getNewPeople();

        return true;
    }

    /*
     * returns factor of people who were fed during current cycle
     * for example if we need to feed 10 people and we only have food for 8
     * then the factor of people who were fed is 8/10 = 0.8
     */
    private double eatFood()
    {
        // TODO consequences of not having enough food
        int foodAvailable = kingdom.getResources().getCount(ResourceName.food);
        int foodUpkeep = kingdom.getFoodUpkeep();

        if (foodAvailable >= foodUpkeep)
        {
            kingdom.getResources().subtractCount(ResourceName.food, foodUpkeep);

            // everyone was fed
            return 1.0;
        }

        // there wasn't food for everyone
        double fedPeopleRatio = (double) foodAvailable / foodUpkeep;
        // TODO real traces
        System.out.println("There wasn't enough food in " + kingdom.getName() + " only " + fedPeopleRatio + "% were fed");
        kingdom.getResources().subtractCount(ResourceName.food, foodAvailable);
        return fedPeopleRatio;
    }

    private void doProduction(double baseProductionRate)
    {
        var productionConfig = kingdom.getConfig().production();

        for (var unitName : UnitName.getProductionUnits())
        {
            var resourceType = productionConfig.getResource(unitName);
            var resourceProduction = kingdom.getUnits().getCount(unitName) * baseProductionRate * productionConfig.getProductionRate(unitName);
            if (unitName == UnitName.blacksmith)
            {
                // TODO have the rate somewhere in the config
                int ironConsumptionPerOneProductionUnit = 1;
                // unfed blacksmith who don't work, will not consume any iron either
                int neededIron = (int) (resourceProduction * ironConsumptionPerOneProductionUnit);
                var maxIronToSpend = Math.min(neededIron, kingdom.getResources().getCount(ResourceName.iron));
                resourceProduction = Math.min(resourceProduction, maxIronToSpend);
                kingdom.getResources().subtractCount(ResourceName.iron, maxIronToSpend);
            }
            int actualProduction = (int) Math.round(resourceProduction * getProductionBonus());
            kingdom.getResources().addCount(resourceType, actualProduction);
        }
    }

    private void getNewPeople()
    {
        var housingCapacity = kingdom.getBuildings().getCount(BuildingName.house) * kingdom.getConfig().buildingCapacity().getCapacity(BuildingName.house);
        var peopleCount = kingdom.getTotalPeopleCount();
        if (housingCapacity > peopleCount)
        {
            kingdom.getResources().addCount(ResourceName.unemployed, housingCapacity - peopleCount);
        } else if (housingCapacity < peopleCount)
        {
            // TODO test
            // TODO fire workers here
            kingdom.getResources().subtractCount(ResourceName.unemployed, peopleCount - housingCapacity);
        }
    }

    private double getProductionBonus()
    {
        var land = kingdom.getResources().getCount(ResourceName.land);
        if (land > 1000)
        {
            return 1.0;
        }

        var landFactor = 1000 - Math.max(100, land); // we don't give a bonus for land below 100 to avoid exploits
        var bonus = getBonusFactorBasedOnLand(landFactor);

        return bonus;
    }

    /**
     * the bonus factor decreases exponentially, max is 5 at 100 land, min is 1 at
     * 1000 land
     * 
     * @param land
     * @return
     */
    private double getBonusFactorBasedOnLand(int land)
    {
        var bonus = 6.5 * Math.exp(-0.0047 * land) - 0.06;
        var minBonus = Math.max(0, bonus);
        return 1 + minBonus;
    }
}