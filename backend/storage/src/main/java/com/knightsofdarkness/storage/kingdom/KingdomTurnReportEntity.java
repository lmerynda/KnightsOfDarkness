package com.knightsofdarkness.storage.kingdom;

import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.knightsofdarkness.common.kingdom.KingdomTurnReport;
import com.knightsofdarkness.common.kingdom.ResourceName;
import com.knightsofdarkness.common.kingdom.UnitName;

import jakarta.persistence.Embeddable;

@Embeddable
public class KingdomTurnReportEntity {
    public int foodConsumed;
    @JdbcTypeCode(SqlTypes.JSON)
    public Map<ResourceName, Integer> resourcesProduced;
    public int arrivingPeople;
    public int exiledPeople;
    public double kingdomSizeProductionBonus;
    public double nourishmentProductionFactor;
    @JdbcTypeCode(SqlTypes.JSON)
    public Map<ResourceName, Double> specialBuildingBonus;
    @JdbcTypeCode(SqlTypes.JSON)
    public Map<UnitName, Integer> professionalsLeaving;

    public KingdomTurnReport toDto()
    {
        var kingdomTurnPassedResults = new KingdomTurnReport();
        kingdomTurnPassedResults.foodConsumed = foodConsumed;
        kingdomTurnPassedResults.resourcesProduced = resourcesProduced;
        kingdomTurnPassedResults.arrivingPeople = arrivingPeople;
        kingdomTurnPassedResults.exiledPeople = exiledPeople;
        kingdomTurnPassedResults.kingdomSizeProductionBonus = kingdomSizeProductionBonus;
        kingdomTurnPassedResults.nourishmentProductionFactor = nourishmentProductionFactor;
        kingdomTurnPassedResults.specialBuildingBonus = specialBuildingBonus;
        kingdomTurnPassedResults.professionalsLeaving = professionalsLeaving;
        return kingdomTurnPassedResults;
    }

    public KingdomTurnReport toDomainModel()
    {
        // TODO change? for now it is the same as Dto
        return toDto();
    }

    public static KingdomTurnReportEntity fromDto(KingdomTurnReport kingdomTurnPassedResults)
    {
        var kingdomTurnReportEntity = new KingdomTurnReportEntity();
        kingdomTurnReportEntity.foodConsumed = kingdomTurnPassedResults.foodConsumed;
        kingdomTurnReportEntity.resourcesProduced = kingdomTurnPassedResults.resourcesProduced;
        kingdomTurnReportEntity.arrivingPeople = kingdomTurnPassedResults.arrivingPeople;
        kingdomTurnReportEntity.exiledPeople = kingdomTurnPassedResults.exiledPeople;
        kingdomTurnReportEntity.kingdomSizeProductionBonus = kingdomTurnPassedResults.kingdomSizeProductionBonus;
        kingdomTurnReportEntity.nourishmentProductionFactor = kingdomTurnPassedResults.nourishmentProductionFactor;
        kingdomTurnReportEntity.specialBuildingBonus = kingdomTurnPassedResults.specialBuildingBonus;
        kingdomTurnReportEntity.professionalsLeaving = kingdomTurnPassedResults.professionalsLeaving;
        return kingdomTurnReportEntity;
    }

    public static KingdomTurnReportEntity fromDomainModel(KingdomTurnReport kingdomTurnPassedResults)
    {
        // TODO change? for now it is the same as Dto
        return fromDto(kingdomTurnPassedResults);
    }

    public String toString()
    {
        return "KingdomTurnPassedResults{" +
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
