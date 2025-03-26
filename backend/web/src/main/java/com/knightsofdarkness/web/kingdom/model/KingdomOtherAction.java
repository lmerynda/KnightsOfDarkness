package com.knightsofdarkness.web.kingdom.model;

import com.knightsofdarkness.web.common.kingdom.LandTransaction;
import com.knightsofdarkness.web.common.kingdom.ResourceName;

public class KingdomOtherAction {

    private final KingdomEntity kingdom;

    public KingdomOtherAction(KingdomEntity kingdom)
    {
        this.kingdom = kingdom;
    }

    public LandTransaction buyLand(int count)
    {
        // TODO tests, check situations where we could potentially go below 0
        // check more sophisticated math like this:
        // https://math.stackexchange.com/questions/296476/formula-for-calculating-the-sum-of-a-series-of-function-results-over-a-specific

        // dumb approach to keep adding next calculated cost for each piece of land
        var availableGold = kingdom.getResources().getCount(ResourceName.gold);
        assert availableGold >= 0;
        var initialLand = kingdom.getResources().getCount(ResourceName.land);

        var transaction = calculateCost(initialLand, count, availableGold);

        kingdom.getResources().subtractCount(ResourceName.gold, transaction.cost());
        kingdom.getResources().addCount(ResourceName.land, transaction.amount());

        return transaction;
    }

    static LandTransaction calculateCost(int initialLand, int count, int availableGold)
    {
        // TODO split this function to separately cover cost and how much can be bought
        // for a given amount of gold
        var power = 2; // linear function f(x) = 1/10 * x^2
        var landPrice = 0;
        var landToBuy = 0;
        var nextCalculatedPrice = (int) Math.round(Math.pow((double) initialLand + 1, power) / 10);
        while (landToBuy < count && landPrice + nextCalculatedPrice <= availableGold)
        {
            landPrice += nextCalculatedPrice;
            landToBuy++;
            var priceFactor = initialLand + landToBuy + 1;
            nextCalculatedPrice = (int) Math.round(Math.pow(priceFactor, power) / 10);
        }

        return new LandTransaction(landToBuy, landPrice);
    }

}
