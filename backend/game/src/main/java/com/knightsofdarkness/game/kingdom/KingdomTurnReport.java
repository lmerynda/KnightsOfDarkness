package com.knightsofdarkness.game.kingdom;

import java.util.HashMap;
import java.util.Map;

public class KingdomTurnReport {
    public int foodConsumed = 0;
    public Map<ResourceName, Integer> resourcesProduced = new HashMap<>();
    public int arrivingPeople = 0;
    public int exiledPeople = 0;
    public double kingdomSizeProductionBonus = 0.0;
    public double nourishmentProductionFactor = 1.0;

    public String toString()
    {
        return "KingdomTurnReport{" +
                "foodConsumed=" + foodConsumed +
                ", resourcesProduced=" + resourcesProduced +
                ", arrivingPeople=" + arrivingPeople +
                ", exiledPeople=" + exiledPeople +
                ", kingdomSizeProductionBonus=" + kingdomSizeProductionBonus +
                ", nourishmentProductionFactor=" + nourishmentProductionFactor +
                '}';
    }
}
