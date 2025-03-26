package com.knightsofdarkness.web.common.kingdom;

import java.util.Optional;

import java.util.ArrayList;
import java.util.List;

import com.knightsofdarkness.web.common.market.MarketOfferDto;

public class KingdomDto {
    public String name;
    public KingdomResourcesDto resources;
    public KingdomBuildingsDto buildings;
    public KingdomUnitsDto units;
    public KingdomDetailsDto details;
    public List<MarketOfferDto> marketOffers;
    public List<KingdomSpecialBuildingDto> specialBuildings;
    public KingdomTurnReport lastTurnReport;
    public List<CarriersOnTheMoveDto> carriersOnTheMove;
    public List<OngoingAttackDto> ongoingAttacks;
    public Optional<String> allianceName;

    public KingdomDto()
    {
        this.name = "unknown";
        this.resources = new KingdomResourcesDto();
        this.buildings = new KingdomBuildingsDto();
        this.units = new KingdomUnitsDto();
        this.marketOffers = new ArrayList<>();
        this.specialBuildings = new ArrayList<>();
        this.lastTurnReport = new KingdomTurnReport();
        this.carriersOnTheMove = new ArrayList<>();
        this.ongoingAttacks = new ArrayList<>();
        this.allianceName = Optional.empty();
        initializeDetails();
    }

    public KingdomDto(String name, KingdomResourcesDto resources, KingdomBuildingsDto buildings, KingdomUnitsDto units, List<MarketOfferDto> marketOffers, List<KingdomSpecialBuildingDto> specialBuildings,
            KingdomTurnReport lastTurnReport, List<CarriersOnTheMoveDto> carriersOnTheMove, List<OngoingAttackDto> ongoingAttacks, Optional<String> allianceName)
    {
        this.name = name;
        this.resources = resources;
        this.buildings = buildings;
        this.units = units;
        this.marketOffers = marketOffers;
        this.specialBuildings = specialBuildings;
        this.lastTurnReport = lastTurnReport;
        this.carriersOnTheMove = carriersOnTheMove;
        this.ongoingAttacks = ongoingAttacks;
        this.allianceName = allianceName;
        initializeDetails();
    }

    private void initializeDetails()
    {
        details = new KingdomDetailsDto();
        details.setCount(KingdomDetailName.usedLand, buildings.countAll());
    }

    public String toString()
    {
        return "KingdomDto{" +
                "name='" + name + '\'' +
                ", resources=" + resources +
                ", buildings=" + buildings +
                ", units=" + units +
                ", details=" + details +
                ", marketOffers=" + marketOffers +
                ", specialBuildings=" + specialBuildings +
                ", lastTurnReport=" + lastTurnReport +
                ", carriersOnTheMove=" + carriersOnTheMove +
                ", ongoingAttacks=" + ongoingAttacks +
                ", allianceName=" + allianceName +
                '}';
    }
}
