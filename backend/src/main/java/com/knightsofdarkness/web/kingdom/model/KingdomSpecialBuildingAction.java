package com.knightsofdarkness.web.kingdom.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

import com.knightsofdarkness.web.common.kingdom.ResourceName;
import com.knightsofdarkness.web.common.kingdom.SpecialBuildingType;
import com.knightsofdarkness.web.game.config.GameConfig;
import com.knightsofdarkness.web.utils.Id;
import com.knightsofdarkness.web.utils.Utils;

public class KingdomSpecialBuildingAction {
    private final KingdomEntity kingdom;
    private final GameConfig gameConfig;

    public KingdomSpecialBuildingAction(KingdomEntity kingdom, GameConfig gameConfig)
    {
        this.kingdom = kingdom;
        this.gameConfig = gameConfig;
    }

    /**
     * why a great work of this kind may not be begun, if it may not
     */
    public Optional<String> whyNotStart(SpecialBuildingType buildingType)
    {
        if (buildingType == null)
        {
            return Optional.of("No such great work");
        }
        if (kingdom.getSpecialBuildings().size() >= gameConfig.common().specialBuildingMaxCount())
        {
            return Optional.of("No place for another great work");
        }
        if (!buildingType.mayBeRaisedTwice() && kingdom.getSpecialBuildings().stream().anyMatch(building -> building.buildingType == buildingType))
        {
            return Optional.of(Utils.format("The kingdom already has a {}, and only warehouses may be raised twice", buildingType.name().replaceAll("([A-Z])", " $1").toLowerCase()));
        }
        return Optional.empty();
    }

    public Optional<KingdomSpecialBuildingEntity> startSpecialBuilding(SpecialBuildingType buildingType)
    {
        if (whyNotStart(buildingType).isPresent())
        {
            return Optional.empty();
        }

        var firstLevelCost = gameConfig.specialBuildingCosts().getCost(buildingType);
        var specialBuilding = new KingdomSpecialBuildingEntity(Id.generate(), kingdom, buildingType, 0, 0, firstLevelCost, false);
        kingdom.getSpecialBuildings().add(specialBuilding);

        return Optional.of(specialBuilding);
    }

    public Optional<KingdomSpecialBuildingEntity> getLowestLevelSpecialBuilding()
    {
        if (kingdom.getSpecialBuildings().isEmpty())
        {
            return Optional.empty();
        }

        return Optional.of(Collections.min(kingdom.getSpecialBuildings(), Comparator.comparingInt(specialBuilding -> specialBuilding.level)));
    }

    /**
     * puts building points into the building, completing at most one level, every level costs twice as much as the previous one
     *
     * @return building points spent
     */
    public int buildSpecialBuilding(KingdomSpecialBuildingEntity specialBuilding, int buildingPoints)
    {
        if (specialBuilding.isMaxLevel())
        {
            return 0;
        }

        var spendable = Math.max(0, Math.min(buildingPoints, kingdom.getResources().getCount(ResourceName.buildingPoints)));
        var buildingPointsRemaining = specialBuilding.buildingPointsRequired - specialBuilding.buildingPointsPut;
        if (spendable < buildingPointsRemaining)
        {
            kingdom.getResources().subtractCount(ResourceName.buildingPoints, spendable);
            specialBuilding.buildingPointsPut += spendable;
            return spendable;
        }

        kingdom.getResources().subtractCount(ResourceName.buildingPoints, buildingPointsRemaining);
        specialBuilding.buildingPointsPut = 0;
        specialBuilding.level++;
        specialBuilding.buildingPointsRequired *= 2;
        if (specialBuilding.level >= gameConfig.common().specialBuildingMaxLevel())
        {
            specialBuilding.isMaxLevel = true;
            specialBuilding.buildingPointsRequired = 0;
        }
        return buildingPointsRemaining;
    }

    public boolean demolishSpecialBuilding(UUID id)
    {
        var specialBuilding = kingdom.getSpecialBuildings().stream().filter(sb -> sb.id.equals(id)).findFirst();
        if (specialBuilding.isEmpty())
        {
            return false;
        }

        kingdom.getSpecialBuildings().remove(specialBuilding.get());
        return true;
    }
}
