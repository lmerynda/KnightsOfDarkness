package com.knightsofdarkness.web.common.kingdom;

import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class KingdomTurnReport {
    public int foodConsumed = 0;
    public Map<ResourceName, Integer> resourcesProduced = new EnumMap<>(ResourceName.class);
    public int arrivingPeople = 0;
    public int exiledPeople = 0;
    public int starvedPeople = 0;
    /** gold paid to the soldiers */
    public int goldUpkeep = 0;
    /** soldiers at home who left because their pay fell short */
    public int desertedSoldiers = 0;
    public int weaponsProductionPercentage = 0;
    public double kingdomSizeProductionBonus = 0.0;
    public double nourishmentProductionFactor = 1.0;
    public Map<ResourceName, Double> specialBuildingBonus = new EnumMap<>(ResourceName.class);
    public Map<UnitName, Integer> professionalsLeaving = new EnumMap<>(UnitName.class);
    /** what fortune brought after the turn, if anything */
    public @Nullable String event;

    public void addLeavingProfessionals(UnitName unit, int count)
    {
        professionalsLeaving.merge(unit, count, Integer::sum);
    }

    // stored as JSON, hibernate compares by value to decide whether the kingdom changed
    @Override
    public boolean equals(Object other)
    {
        if (this == other)
            return true;
        if (!(other instanceof KingdomTurnReport that))
            return false;
        return foodConsumed == that.foodConsumed && arrivingPeople == that.arrivingPeople && exiledPeople == that.exiledPeople && starvedPeople == that.starvedPeople
                && goldUpkeep == that.goldUpkeep && desertedSoldiers == that.desertedSoldiers
                && weaponsProductionPercentage == that.weaponsProductionPercentage && Double.compare(kingdomSizeProductionBonus, that.kingdomSizeProductionBonus) == 0
                && Double.compare(nourishmentProductionFactor, that.nourishmentProductionFactor) == 0 && Objects.equals(resourcesProduced, that.resourcesProduced)
                && Objects.equals(specialBuildingBonus, that.specialBuildingBonus) && Objects.equals(professionalsLeaving, that.professionalsLeaving) && Objects.equals(event, that.event);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(foodConsumed, resourcesProduced, arrivingPeople, exiledPeople, starvedPeople, goldUpkeep, desertedSoldiers, weaponsProductionPercentage, kingdomSizeProductionBonus, nourishmentProductionFactor,
                specialBuildingBonus, professionalsLeaving, event);
    }

    @Override
    public String toString()
    {
        return "KingdomTurnReport{" +
                "foodConsumed=" + foodConsumed +
                ", resourcesProduced=" + resourcesProduced +
                ", arrivingPeople=" + arrivingPeople +
                ", exiledPeople=" + exiledPeople +
                ", starvedPeople=" + starvedPeople +
                ", goldUpkeep=" + goldUpkeep +
                ", desertedSoldiers=" + desertedSoldiers +
                ", weaponsProductionPercentage=" + weaponsProductionPercentage +
                ", kingdomSizeProductionBonus=" + kingdomSizeProductionBonus +
                ", nourishmentProductionFactor=" + nourishmentProductionFactor +
                ", specialBuildingBonus=" + specialBuildingBonus +
                ", professionalsLeaving=" + professionalsLeaving +
                ", event=" + event +
                '}';
    }
}
