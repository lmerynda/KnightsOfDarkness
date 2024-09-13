package com.knightsofdarkness.storage.kingdom;

import com.knightsofdarkness.common_legacy.kingdom.KingdomBuildingsDto;
import com.knightsofdarkness.game.kingdom.BuildingName;
import com.knightsofdarkness.game.kingdom.KingdomBuildings;

import jakarta.persistence.Embeddable;

@Embeddable
class KingdomBuildingsEntity {
    int houses;
    int goldMines;
    int ironMines;
    int workshops;
    int farms;
    int markets;
    int barracks;
    int guardHouses;
    int spyGuilds;
    int towers;
    int castles;

    public KingdomBuildingsEntity()
    {
    }

    public KingdomBuildingsEntity(int houses, int goldMines, int ironMines, int workshops, int farms, int markets, int barracks, int guardHouses, int spyGuilds, int towers, int castles)
    {
        this.houses = houses;
        this.goldMines = goldMines;
        this.ironMines = ironMines;
        this.workshops = workshops;
        this.farms = farms;
        this.markets = markets;
        this.barracks = barracks;
        this.guardHouses = guardHouses;
        this.spyGuilds = spyGuilds;
        this.towers = towers;
        this.castles = castles;
    }

    public KingdomBuildingsDto toDto()
    {
        return new KingdomBuildingsDto(houses, goldMines, ironMines, workshops, farms, markets, barracks, guardHouses, spyGuilds, towers, castles);
    }

    public KingdomBuildings toDomainModel()
    {
        var kingdomBuildings = new KingdomBuildings();
        kingdomBuildings.setCount(BuildingName.house, houses);
        kingdomBuildings.setCount(BuildingName.goldMine, goldMines);
        kingdomBuildings.setCount(BuildingName.ironMine, ironMines);
        kingdomBuildings.setCount(BuildingName.workshop, workshops);
        kingdomBuildings.setCount(BuildingName.farm, farms);
        kingdomBuildings.setCount(BuildingName.market, markets);
        kingdomBuildings.setCount(BuildingName.barracks, barracks);
        kingdomBuildings.setCount(BuildingName.guardHouse, guardHouses);
        kingdomBuildings.setCount(BuildingName.spyGuild, spyGuilds);
        kingdomBuildings.setCount(BuildingName.tower, towers);
        kingdomBuildings.setCount(BuildingName.castle, castles);
        return kingdomBuildings;
    }

    public static KingdomBuildingsEntity fromDomainModel(KingdomBuildings kingdomBuildings)
    {
        return new KingdomBuildingsEntity(
                kingdomBuildings.getCount(BuildingName.house),
                kingdomBuildings.getCount(BuildingName.goldMine),
                kingdomBuildings.getCount(BuildingName.ironMine),
                kingdomBuildings.getCount(BuildingName.workshop),
                kingdomBuildings.getCount(BuildingName.farm),
                kingdomBuildings.getCount(BuildingName.market),
                kingdomBuildings.getCount(BuildingName.barracks),
                kingdomBuildings.getCount(BuildingName.guardHouse),
                kingdomBuildings.getCount(BuildingName.spyGuild),
                kingdomBuildings.getCount(BuildingName.tower),
                kingdomBuildings.getCount(BuildingName.castle));
    }
}
