package com.knightsofdarkness.common;

import java.util.ArrayList;
import java.util.List;

import com.knightsofdarkness.game.gameconfig.GameConfig;
import com.knightsofdarkness.game.kingdom.Kingdom;
import com.knightsofdarkness.game.kingdom.KingdomSpecialBuilding;
import com.knightsofdarkness.game.kingdom.KingdomTurnReport;

public class KingdomDto {
    public String name;
    public KingdomResourcesDto resources;
    public KingdomBuildingsDto buildings;
    public KingdomUnitsDto units;
    public List<MarketOfferDto> marketOffers;
    public List<KingdomSpecialBuildingDto> specialBuildings;
    public KingdomTurnReport lastTurnReport;

    public KingdomDto()
    {
    }

    public KingdomDto(String name, KingdomResourcesDto resources, KingdomBuildingsDto buildings, KingdomUnitsDto units, List<MarketOfferDto> marketOffers, List<KingdomSpecialBuildingDto> specialBuildings,
            KingdomTurnReport lastTurnReport)
    {
        this.name = name;
        this.resources = resources;
        this.buildings = buildings;
        this.units = units;
        this.marketOffers = marketOffers;
        this.specialBuildings = specialBuildings;
        this.lastTurnReport = lastTurnReport;
        setDetails();
    }

    private void setDetails()
    {
        resources.usedLand = buildings.countAll();
    }

    public Kingdom toDomain(GameConfig config)
    {
        // TODO hehe, no, go back and fix this...
        if (lastTurnReport == null)
        {
            lastTurnReport = new KingdomTurnReport();
        }

        // fix this too
        if (specialBuildings == null)
        {
            specialBuildings = new ArrayList<>();
        }
        List<KingdomSpecialBuilding> specialBuildings = this.specialBuildings.stream().map(KingdomSpecialBuildingDto::toDomain).toList();
        return new Kingdom(name, config, resources.toDomain(), buildings.toDomain(), specialBuildings, units.toDomain(), lastTurnReport);
    }

    public static KingdomDto fromDomain(Kingdom kingdom)
    {
        List<KingdomSpecialBuildingDto> specialBuildings = kingdom.getSpecialBuildings().stream().map(KingdomSpecialBuildingDto::fromDomain).toList();
        return new KingdomDto(kingdom.getName(), KingdomResourcesDto.fromDomain(kingdom.getResources()), KingdomBuildingsDto.fromDomain(kingdom.getBuildings()), KingdomUnitsDto.fromDomain(kingdom.getUnits()), new ArrayList<>(),
                specialBuildings, kingdom.getLastTurnReport());
    }

    public String toString()
    {
        return "KingdomDto{" +
                "name='" + name +
                ", resources=" + resources +
                ", buildings=" + buildings +
                ", units=" + units +
                ", marketOffers=" + marketOffers +
                ", specialBuildings=" + specialBuildings +
                '}';
    }
}
