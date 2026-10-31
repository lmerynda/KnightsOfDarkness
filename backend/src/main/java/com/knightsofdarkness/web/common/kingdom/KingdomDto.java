package com.knightsofdarkness.web.common.kingdom;

import java.util.Optional;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import com.knightsofdarkness.web.common.market.MarketOfferDto;

public class KingdomDto
{
    public String name;
    public EnumMap<ResourceName, Integer> resources;
    public EnumMap<BuildingName, Integer> buildings;
    public KingdomUnitsDto units;
    public List<MarketOfferDto> marketOffers;
    public List<KingdomSpecialBuildingDto> specialBuildings;
    public KingdomTurnReport lastTurnReport;
    public List<CarriersOnTheMoveDto> carriersOnTheMove;
    public List<OngoingAttackDto> ongoingAttacks;
    public Optional<String> allianceName;
    public int protectionTicks;
    public List<ReinforcementDto> reinforcements = new ArrayList<>();
    public List<ReinforcementDto> garrisons = new ArrayList<>();

    public KingdomDto()
    {
        this.name = "unknown";
        this.resources = new EnumMap<>(ResourceName.class);
        this.buildings = new EnumMap<>(BuildingName.class);
        this.units = new KingdomUnitsDto();
        this.marketOffers = new ArrayList<>();
        this.specialBuildings = new ArrayList<>();
        this.lastTurnReport = new KingdomTurnReport();
        this.carriersOnTheMove = new ArrayList<>();
        this.ongoingAttacks = new ArrayList<>();
        this.allianceName = Optional.empty();
    }

    public KingdomDto(String name, KingdomResourcesDto resources, KingdomBuildingsDto buildings, KingdomUnitsDto units, List<MarketOfferDto> marketOffers, List<KingdomSpecialBuildingDto> specialBuildings, KingdomTurnReport lastTurnReport,
            List<CarriersOnTheMoveDto> carriersOnTheMove, List<OngoingAttackDto> ongoingAttacks, Optional<String> allianceName)
    {
        this.name = name;
        this.resources = new EnumMap<>(resources.getResources());
        this.buildings = new EnumMap<>(buildings.getBuildings());
        this.units = units;
        this.marketOffers = marketOffers;
        this.specialBuildings = specialBuildings;
        this.lastTurnReport = lastTurnReport;
        this.carriersOnTheMove = carriersOnTheMove;
        this.ongoingAttacks = ongoingAttacks;
        this.allianceName = allianceName;
    }

    public String toString()
    {
        return "KingdomDto{" +
                "name='" + name + '\'' +
                ", resources=" + resources +
                ", buildings=" + buildings +
                ", units=" + units +
                ", marketOffers=" + marketOffers +
                ", specialBuildings=" + specialBuildings +
                ", lastTurnReport=" + lastTurnReport +
                ", carriersOnTheMove=" + carriersOnTheMove +
                ", ongoingAttacks=" + ongoingAttacks +
                ", allianceName=" + allianceName +
                '}';
    }
}
