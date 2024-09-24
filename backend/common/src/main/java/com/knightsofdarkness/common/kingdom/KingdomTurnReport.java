package com.knightsofdarkness.common.kingdom;

import java.util.EnumMap;
import java.util.Map;

public class KingdomTurnReport {
    public int foodConsumed = 0;
    public Map<ResourceName, Integer> resourcesProduced = new EnumMap<>(ResourceName.class);
    public int arrivingPeople = 0;
    public int exiledPeople = 0;
    public double kingdomSizeProductionBonus = 0.0;
    public double nourishmentProductionFactor = 1.0;
    public Map<ResourceName, Double> specialBuildingBonus = new EnumMap<>(ResourceName.class);
    public Map<UnitName, Integer> professionalsLeaving = new EnumMap<>(UnitName.class);

    public void addLeavingProfessionals(UnitName unit, int count)
    {
        professionalsLeaving.merge(unit, count, Integer::sum);
    }

    public String toString()
    {
        return "KingdomTurnReport{" +
                "foodConsumed=" + foodConsumed +
                ", resourcesProduced=" + resourcesProduced +
                ", arrivingPeople=" + arrivingPeople +
                ", exiledPeople=" + exiledPeople +
                ", kingdomSizeProductionBonus=" + kingdomSizeProductionBonus +
                ", nourishmentProductionFactor=" + nourishmentProductionFactor +
                ", specialBuildingBonus=" + specialBuildingBonus +
                ", professionalsLeaving=" + professionalsLeaving +
                '}';
    }
}
