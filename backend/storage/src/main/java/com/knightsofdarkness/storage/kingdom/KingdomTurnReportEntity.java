package com.knightsofdarkness.storage.kingdom;

import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.knightsofdarkness.game.kingdom.KingdomTurnReport;
import com.knightsofdarkness.game.kingdom.ResourceName;

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

    public KingdomTurnReport toDto()
    {
        var kingdomTurnPassedResults = new KingdomTurnReport();
        kingdomTurnPassedResults.foodConsumed = foodConsumed;
        kingdomTurnPassedResults.resourcesProduced = resourcesProduced;
        kingdomTurnPassedResults.arrivingPeople = arrivingPeople;
        kingdomTurnPassedResults.exiledPeople = exiledPeople;
        kingdomTurnPassedResults.kingdomSizeProductionBonus = kingdomSizeProductionBonus;
        kingdomTurnPassedResults.nourishmentProductionFactor = nourishmentProductionFactor;
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
                '}';
    }

}
