package com.knightsofdarkness.web.kingdom.model;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Optional;

import com.knightsofdarkness.web.common.kingdom.BuildingName;
import com.knightsofdarkness.web.common.kingdom.KingdomDetailsDto;
import com.knightsofdarkness.web.common.kingdom.KingdomTurnReport;
import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.kingdom.UnitName;
import com.knightsofdarkness.web.common.market.MarketResource;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.kingdom.IKingdomInteractor;

public class KingdomDetailsCalculator {
    private final GameConfig gameConfig;
    private final KingdomDetailsProvider detailsProvider;

    public KingdomDetailsCalculator(GameConfig gameConfig)
    {
        this.gameConfig = gameConfig;
        this.detailsProvider = new KingdomDetailsProvider(gameConfig);
    }

    public KingdomDetailsDto calculate(KingdomEntity kingdom, int weaponsProductionPercentage)
    {
        var capacity = new EnumMap<BuildingName, Integer>(BuildingName.class);
        var occupancy = new EnumMap<BuildingName, Integer>(BuildingName.class);
        for (var building : BuildingName.values())
        {
            var housedUnits = Arrays.stream(UnitName.values()).filter(unit -> BuildingName.getByUnit(unit).equals(Optional.of(building))).toList();
            if (!housedUnits.isEmpty())
            {
                capacity.put(building, kingdom.getBuildingCapacity(building, gameConfig));
                occupancy.put(building, housedUnits.stream().mapToInt(kingdom.getUnits()::getTotalCount).sum());
            }
        }

        var land = kingdom.getResources().getCount(ResourceName.land);
        var nextLandPrice = KingdomOtherAction.calculateCost(land, 1, Integer.MAX_VALUE).cost();

        return new KingdomDetailsDto(kingdom.getOccupiedLand(), detailsProvider.getHousingCapacity(kingdom), kingdom.getHousedPeopleCount(), capacity, occupancy,
                detailsProvider.getFoodUpkeep(kingdom), detailsProvider.getDefensePower(kingdom), detailsProvider.getCounterIntelligence(kingdom), nextLandPrice,
                forecast(kingdom, weaponsProductionPercentage));
    }

    /**
     * plays the next turn on a copy of the kingdom, armies and carriers on the move are left out
     */
    KingdomTurnReport forecast(KingdomEntity kingdom, int weaponsProductionPercentage)
    {
        var copy = KingdomEntity.fromDto(kingdom.toDto());
        copy.getResources().setCount(ResourceName.turns, 1);
        var result = new KingdomTurnAction(copy, new IdleInteractor(), gameConfig, detailsProvider).passTurn(weaponsProductionPercentage);
        return result.turnReport().orElseGet(KingdomTurnReport::new);
    }

    private static class IdleInteractor implements IKingdomInteractor {
        @Override
        public void transferResources(KingdomEntity from, String to, MarketResource resource, int count)
        {
            // a forecast does not touch other kingdoms
        }

        @Override
        public void resolveAttack(KingdomEntity kingdom, KingdomOngoingAttackEntity attack)
        {
            // a forecast does not touch other kingdoms
        }

        @Override
        public boolean waitsAtBorder(KingdomOngoingAttackEntity attack)
        {
            // a forecast does not look at other kingdoms' armies
            return false;
        }

        @Override
        public void reinforcementsArrived(KingdomReinforcementEntity reinforcement)
        {
            // a forecast does not touch other kingdoms
        }
    }
}
